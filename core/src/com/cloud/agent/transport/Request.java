/**
 *  Copyright (C) 2010 Cloud.com, Inc.  All rights reserved.
 * 
 * This software is licensed under the GNU General Public License v3 or later.
 * 
 * It is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package com.cloud.agent.transport;

import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.Command;
import com.cloud.agent.api.SecStorageFirewallCfgCommand.PortConfig;
import com.cloud.exception.UnsupportedVersionException;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.Pair;
import com.cloud.utils.exception.CloudRuntimeException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

/**
 * Request is a simple wrapper around command and answer to add sequencing,
 * versioning, and flags. Note that the version here represents the changes
 * in the over the wire protocol.  For example, if we decide to not use Gson.
 * It does not version the changes in the actual commands.  That's expected
 * to be done by adding new classes to the command and answer list.
 *
 * A request looks as follows:
 *   1. Version - 1 byte;
 *   2. Flags - 3 bytes;
 *   3. Sequence - 8 bytes;
 *   4. Length - 4 bytes;
 *   5. ManagementServerId - 8 bytes;
 *   6. AgentId - 8 bytes;
 *   7. Data Package.
 *
 */
public class Request {
    private static final Logger s_logger = Logger.getLogger(Request.class);

    public enum Version {
        v1, // using gson to marshall
        v2, // now using gson as marshalled.
        v3; // Adding routing information into the Request data structure.

        public static Version get(final byte ver) throws UnsupportedVersionException {
            for (final Version version : Version.values()) {
                if (ver == version.ordinal()) {
                    return version;
                }
            }
            throw new UnsupportedVersionException("Can't lookup version: " + ver, UnsupportedVersionException.UnknownVersion);
        }
    };

    protected static final short FLAG_RESPONSE = 0x0;
    protected static final short FLAG_REQUEST = 0x1;
    protected static final short FLAG_STOP_ON_ERROR = 0x2;
    protected static final short FLAG_IN_SEQUENCE = 0x4;
    protected static final short FLAG_REVERT_ON_ERROR = 0x8;
    protected static final short FLAG_FROM_SERVER = 0x20;
    protected static final short FLAG_CONTROL = 0x40;

    protected static final GsonBuilder s_gBuilder;
    static {
        s_gBuilder = new GsonBuilder();
        s_gBuilder.registerTypeAdapter(Command[].class, new ArrayTypeAdaptor<Command>());
        s_gBuilder.registerTypeAdapter(Answer[].class, new ArrayTypeAdaptor<Answer>());
//        final Type listType = new TypeToken<List<VolumeVO>>() {}.getType();
//        s_gBuilder.registerTypeAdapter(listType, new VolListTypeAdaptor());
        s_gBuilder.registerTypeAdapter(new TypeToken<List<PortConfig>>() {}.getType(), new PortConfigListTypeAdaptor());
        s_gBuilder.registerTypeAdapter(new TypeToken<Pair<Long, Long>>() {}.getType(), new NwGroupsCommandTypeAdaptor());
        s_logger.info("Builder inited.");
    }

    public static GsonBuilder initBuilder() {
        return s_gBuilder;
    }

    protected Version                 _ver;
    protected long                    _seq;
    protected short                   _flags;
    protected long                    _mgmtId;
    protected long                    _agentId;
    protected Command[]               _cmds;
    protected String                  _content;

    protected Request() {
    }

    protected Request(Version ver, long seq, long agentId, long mgmtId, short flags, final Command[] cmds) {
        _ver = ver;
        _cmds = cmds;
        _flags = flags;
        _seq = seq;
        _agentId = agentId;
        _mgmtId = mgmtId;
        setInSequence(cmds);
    }
    
    protected Request(Version ver, long seq, long agentId, long mgmtId, short flags, final String content) {
        this(ver, seq, agentId, mgmtId, flags, (Command[])null);
        _content = content;
    }
    
    public Request(long seq, long agentId, long mgmtId, final Command command, boolean fromServer) {
        this(seq, agentId, mgmtId, new Command[] {command}, true, fromServer, true);
    }
    
    public Request(long seq, long agentId, long mgmtId, Command[] cmds, boolean stopOnError, boolean fromServer, boolean revert) {
        this(Version.v3, seq, agentId, mgmtId, (short)0, cmds);
        setStopOnError(stopOnError);
        setFromServer(fromServer);
        setRevertOnError(revert);
    }
    
    protected void setInSequence(Command[] cmds) {
        if (cmds == null) {
            return;
        }
        for (Command cmd : cmds) {
            if (cmd.executeInSequence()) {
                setInSequence(true);
                break;
            }
        }
    }
    
    protected Request(final Request that, final Command[] cmds) {
        this._ver = that._ver;
        this._seq = that._seq;
        setInSequence(that.executeInSequence());
        setStopOnError(that.stopOnError());
        this._cmds = cmds;
        this._mgmtId = that._mgmtId;
        this._agentId = that._agentId;
        setFromServer(!that.isFromServer());
    }
    
    private final void setStopOnError(boolean stopOnError) {
        _flags |= (stopOnError ? FLAG_STOP_ON_ERROR : 0);
    }
    
    private final void setInSequence(boolean inSequence) {
        _flags |= (inSequence ? FLAG_IN_SEQUENCE : 0);
    }
    
    
    public boolean isControl() {
        return (_flags & FLAG_CONTROL) > 0; 
    }
    
    public void setControl(boolean control) {
        _flags |= (control ? FLAG_CONTROL : 0);
    }
    
    public boolean revertOnError() {
        return (_flags & FLAG_CONTROL) > 0;
    }
    
    private final void setRevertOnError(boolean revertOnError) {
        _flags |= (revertOnError ? FLAG_REVERT_ON_ERROR : 0);
    }
    
    private final void setFromServer(boolean fromServer) {
        _flags |= (fromServer ? FLAG_FROM_SERVER : 0);
    }
    
    public long getManagementServerId() {
        return _mgmtId;
    }

    public boolean isFromServer() {
        return (_flags & FLAG_FROM_SERVER) > 0;
    }
    
    public Version getVersion() {
        return _ver;
    }
    
    public void setAgentId(long agentId) {
        _agentId = agentId;
    }

    public boolean executeInSequence() {
        return (_flags & FLAG_IN_SEQUENCE) > 0;
    }

    public long getSequence() {
        return _seq;
    }

    public boolean stopOnError() {
        return (_flags & FLAG_STOP_ON_ERROR) > 0;
    }

    public Command getCommand() {
    	getCommands();
        return _cmds[0];
    }

    public Command[] getCommands() {
    	if (_cmds == null) {
            final Gson json = s_gBuilder.create();
    		_cmds = json.fromJson(_content, Command[].class);
    	}
		return _cmds;
    }

    /**
     * Use this only surrounded by debug.
     */
    @Override
    public String toString() {
        String content = _content;
        if (content == null) {
            final Gson gson = s_gBuilder.create();
            try {
            	content = gson.toJson(_cmds);
            } catch(Throwable e) {
            	s_logger.error("Gson serialization error on Request.toString() " + getClass().getCanonicalName(), e);
            }
        }
        final StringBuilder buffer = new StringBuilder();
        buffer.append("{ ").append(getType());
        buffer.append(", Seq: ").append(_seq).append(", Ver: ").append(_ver.toString()).append(", MgmtId: ").append(_mgmtId).append(", AgentId: ").append(_agentId).append(", Flags: ").append(Integer.toBinaryString(getFlags()));
        buffer.append(", ").append(content).append(" }");
        return buffer.toString();
    }

    protected String getType() {
        return "Cmd ";
    }

    protected ByteBuffer serializeHeader(final int contentSize) {
        final ByteBuffer buffer = ByteBuffer.allocate(32);
        buffer.put(getVersionInByte());
        buffer.put((byte)0);
        buffer.putShort(getFlags());
        buffer.putLong(_seq);
        buffer.putInt(contentSize);
        buffer.putLong(_mgmtId);
        buffer.putLong(_agentId);
        buffer.flip();

        return buffer;
    }

    public ByteBuffer[] toBytes() {
        final Gson gson = s_gBuilder.create();
        final ByteBuffer[] buffers = new ByteBuffer[2];

        if (_content == null) {
        	_content = gson.toJson(_cmds, _cmds.getClass());
        }
        buffers[1] = ByteBuffer.wrap(_content.getBytes());
        buffers[0] = serializeHeader(buffers[1].capacity());

        return buffers;
    }

    public byte[] getBytes() {
        final ByteBuffer[] buffers = toBytes();
        final int len1 = buffers[0].remaining();
        final int len2 = buffers[1].remaining();
        final byte[] bytes = new byte[len1 + len2];
        buffers[0].get(bytes, 0, len1);
        buffers[1].get(bytes, len1, len2);
        return bytes;
    }

    protected byte getVersionInByte() {
        return (byte)_ver.ordinal();
    }

    protected short getFlags() {
        return (short)(((this instanceof Response) ? FLAG_RESPONSE : FLAG_REQUEST) | _flags);
    }
    
    public void log(long agentId, String msg) {
        if (!s_logger.isDebugEnabled()) {
            return;
        }
        
        StringBuilder buf = new StringBuilder("Seq ");
        buf.append(agentId).append("-").append(_seq).append(": ");
        boolean debug = false;
        if (_cmds != null) {
            for (Command cmd : _cmds) {
                if (!cmd.logTrace()) {
                    debug = true;
                    break;
                }
            }
        } else {
            debug = true;
        }
        
        buf.append(msg).append(toString());
        
        if (executeInSequence() || debug) {
            s_logger.debug(buf.toString());
        } else {
            s_logger.trace(buf.toString());
        }
    }

    /**
     * Factory method for Request and Response.  It expects the bytes to be
     * correctly formed so it's possible that it throws underflow exceptions
     * but you shouldn't be concerned about that since that all bytes sent in
     * should already be formatted correctly.
     *
     * @param bytes bytes to be converted.
     * @return Request or Response depending on the data.
     * @throws ClassNotFoundException if the Command or Answer can not be formed.
     * @throws
     */
    public static Request parse(final byte[] bytes) throws ClassNotFoundException, UnsupportedVersionException {
        final ByteBuffer buff = ByteBuffer.wrap(bytes);
        final byte ver = buff.get();
        final Version version = Version.get(ver);
        if (version.ordinal() < Version.v3.ordinal()) {
            throw new UnsupportedVersionException("This version is no longer supported: " + version.toString(), UnsupportedVersionException.IncompatibleVersion);
        }
        final byte reserved = buff.get(); // tossed away for now.
        final short flags = buff.getShort();
        final boolean isRequest = (flags & FLAG_REQUEST) > 0;

        final long seq = buff.getLong();
        final int size = buff.getInt();
        final long mgmtId = buff.getLong();
        final long agentId = buff.getLong();

        byte[] command = null;
        int offset = 0;
        if (buff.hasArray()) {
            command = buff.array();
            offset = buff.arrayOffset() + buff.position();
        } else {
            command = new byte[buff.remaining()];
            buff.get(command);
            offset = 0;
        }

        final String content = new String(command, offset, command.length - offset);

        if (isRequest) {
            return new Request(version, seq, agentId, mgmtId, flags, content);
        } else {
            return new Response(Version.get(ver), seq, agentId, mgmtId, flags, content);
        }
    }

    public long getAgentId() {
    	return _agentId;
    }
    
    public static boolean requiresSequentialExecution(final byte[] bytes) {
        return (bytes[3] & FLAG_IN_SEQUENCE) > 0;
    }
    
    public static Version getVersion(final byte[] bytes) throws UnsupportedVersionException {
    	try {
    		return Version.get(bytes[0]);
    	} catch (UnsupportedVersionException e) {
    		throw new CloudRuntimeException("Unsupported version: " + bytes[0]);
    	}
    }
    
    public static long getManagementServerId(final byte[] bytes) {
    	return NumbersUtil.bytesToLong(bytes, 16);
    }
    
    public static long getAgentId(final byte[] bytes) {
    	return NumbersUtil.bytesToLong(bytes, 24);
    }
    
    public static boolean fromServer(final byte[] bytes) {
    	return (bytes[3] & FLAG_FROM_SERVER)  > 0;
    }
    
    public static boolean isRequest(final byte[] bytes) {
    	return (bytes[3] & FLAG_REQUEST) > 0;
    }
    
    public static long getSequence(final byte[] bytes) {
    	return NumbersUtil.bytesToLong(bytes, 4);
    }
    
    public static boolean isControl(final byte[] bytes) {
        return (bytes[3] & FLAG_CONTROL) > 0;
    }
    
    public static class NwGroupsCommandTypeAdaptor implements JsonDeserializer<Pair<Long, Long>>, JsonSerializer<Pair<Long,Long>> {

        public NwGroupsCommandTypeAdaptor() {
        }
        
        @Override
        public JsonElement serialize(Pair<Long, Long> src,
                java.lang.reflect.Type typeOfSrc, JsonSerializationContext context) {
            JsonArray array = new JsonArray();
            Gson json = s_gBuilder.create();
            if(src.first() != null) {
                array.add(json.toJsonTree(src.first()));
            } else {
                array.add(new JsonNull());
            }
            
            if (src.second() != null) {
                array.add(json.toJsonTree(src.second()));
            } else {
                array.add(new JsonNull());
            }
            
            return array;
        }

        @Override
        public Pair<Long, Long> deserialize(JsonElement json,
                java.lang.reflect.Type type, JsonDeserializationContext context)
                throws JsonParseException {
            Pair<Long, Long> pairs = new Pair<Long, Long>(null, null);
            JsonArray array = json.getAsJsonArray();
            if (array.size() != 2) {
                return pairs;
            }
            JsonElement element = array.get(0);
            if (!element.isJsonNull()) {
                pairs.first(element.getAsLong());
            }

            element = array.get(1);
            if (!element.isJsonNull()) {
                pairs.second(element.getAsLong());
            }

            return pairs;
        }
        
    }
    
    public static class PortConfigListTypeAdaptor implements JsonDeserializer<List<PortConfig>>, JsonSerializer<List<PortConfig>> {

        public PortConfigListTypeAdaptor() {
        }

        @Override
        public JsonElement serialize(List<PortConfig> src, Type typeOfSrc, JsonSerializationContext context) {
            if (src.size() == 0) {
                s_logger.info("Returning JsonNull");
                return new JsonNull();
            }
            Gson json = s_gBuilder.create();
            s_logger.debug("Returning gson tree");
            JsonArray array = new JsonArray();
            for (PortConfig pc : src) {
                array.add(json.toJsonTree(pc));
            }

            return array;
        }

        @Override
        public List<PortConfig> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            if (json.isJsonNull()) {
                return new ArrayList<PortConfig>();
            }
            Gson jsonp = s_gBuilder.create();
            List<PortConfig> pcs = new ArrayList<PortConfig>();
            JsonArray array = json.getAsJsonArray();
            Iterator<JsonElement> it = array.iterator();
            while (it.hasNext()) {
                JsonElement element = it.next();
                pcs.add(jsonp.fromJson(element, PortConfig.class));
            }
            return pcs;
        }
    }
}
