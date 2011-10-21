package com.cloud.agent.transport;

import java.nio.ByteBuffer;

import junit.framework.TestCase;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assert;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.Command;
import com.cloud.agent.api.GetHostStatsCommand;
import com.cloud.agent.api.SecStorageFirewallCfgCommand;
import com.cloud.agent.api.UpdateHostPasswordCommand;
import com.cloud.agent.api.storage.DownloadAnswer;
import com.cloud.agent.api.storage.DownloadCommand;
import com.cloud.exception.UnsupportedVersionException;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.serializer.GsonHelper;
import com.cloud.storage.Storage.ImageFormat;
import com.cloud.storage.Storage.TemplateType;
import com.cloud.storage.VMTemplateStorageResourceAssoc.Status;
import com.cloud.storage.VMTemplateVO;

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

public class RequestTest extends TestCase {
    private static final Logger s_logger = Logger.getLogger(RequestTest.class);

    public void testSerDeser() {
        s_logger.info("Testing serializing and deserializing works as expected");
        
        s_logger.info("UpdateHostPasswordCommand should have two parameters that doesn't show in logging");
        UpdateHostPasswordCommand cmd1 = new UpdateHostPasswordCommand("abc", "def");
        s_logger.info("SecStorageFirewallCfgCommand has a context map that shouldn't show up in debug level");
        SecStorageFirewallCfgCommand cmd2 = new SecStorageFirewallCfgCommand();
        s_logger.info("GetHostStatsCommand should not show up at all in debug level");
        GetHostStatsCommand cmd3 = new GetHostStatsCommand("hostguid", "hostname", 101);
        cmd2.addPortConfig("abc", "24", true, "eth0");
        cmd2.addPortConfig("127.0.0.1", "44", false, "eth1");
        Request sreq = new Request(2, 3, new Command[] { cmd1, cmd2, cmd3 }, true, true);
        sreq.setSequence(892403717);

        Logger logger = Logger.getLogger(GsonHelper.class);
        Level level = logger.getLevel();

        logger.setLevel(Level.DEBUG);
        String log = sreq.log("Debug", true, Level.DEBUG);
        assert (log.contains(UpdateHostPasswordCommand.class.getSimpleName()));
        assert (log.contains(SecStorageFirewallCfgCommand.class.getSimpleName()));
        assert (!log.contains(GetHostStatsCommand.class.getSimpleName()));
        assert (!log.contains("username"));
        assert (!log.contains("password"));

        logger.setLevel(Level.TRACE);
        log = sreq.log("Trace", true, Level.TRACE);
        assert (log.contains(UpdateHostPasswordCommand.class.getSimpleName()));
        assert (log.contains(SecStorageFirewallCfgCommand.class.getSimpleName()));
        assert (log.contains(GetHostStatsCommand.class.getSimpleName()));
        assert (!log.contains("username"));
        assert (!log.contains("password"));

        logger.setLevel(Level.INFO);
        log = sreq.log("Info", true, Level.INFO);
        assert (log == null);

        logger.setLevel(level);

        byte[] bytes = sreq.getBytes();
        
        assert Request.getSequence(bytes) == 892403717;
        assert Request.getManagementServerId(bytes) == 3;
        assert Request.getAgentId(bytes) == 2;
        assert Request.getViaAgentId(bytes) == 2;
        Request creq = null;
        try {
            creq = Request.parse(bytes);
        } catch (ClassNotFoundException e) {
            s_logger.error("Unable to parse bytes: ", e);
        } catch (UnsupportedVersionException e) {
            s_logger.error("Unable to parse bytes: ", e);
        }

        assert creq != null : "Couldn't get the request back";

        compareRequest(creq, sreq);

        Answer ans = new Answer(cmd1, true, "No Problem");
        Response cresp = new Response(creq, ans);

        bytes = cresp.getBytes();

        Response sresp = null;
        try {
            sresp = Response.parse(bytes);
        } catch (ClassNotFoundException e) {
            s_logger.error("Unable to parse bytes: ", e);
        } catch (UnsupportedVersionException e) {
            s_logger.error("Unable to parse bytes: ", e);
        }

        assert sresp != null : "Couldn't get the response back";

        compareRequest(cresp, sresp);
    }

    public void testDownload() {
        s_logger.info("Testing Download answer");
        VMTemplateVO template = new VMTemplateVO(1, "templatename", ImageFormat.QCOW2, true, true, true, TemplateType.USER, "url", true, 32, 1, "chksum", "displayText", true, 30, true,
                HypervisorType.KVM);
        DownloadCommand cmd = new DownloadCommand("secUrl", template, 30000000l);
        Request req = new Request(1, 1, cmd, true);

        req.logD("Debug for Download");


        DownloadAnswer answer = new DownloadAnswer("jobId", 50, "errorString", Status.ABANDONED, "filesystempath", "installpath", 10000000, 20000000);
        Response resp = new Response(req, answer);
        resp.logD("Debug for Download");

    }

    public void testCompress() {
        s_logger.info("testCompress");
        int len = 800000;
        ByteBuffer inputBuffer = ByteBuffer.allocate(len);
        for (int i = 0; i < len; i ++) {
            inputBuffer.array()[i] = 1;
        }
        inputBuffer.limit(len);
        ByteBuffer compressedBuffer = ByteBuffer.allocate(len);
        compressedBuffer = Request.doCompress(inputBuffer, len);
        s_logger.info("compressed length: " + compressedBuffer.limit());
        ByteBuffer decompressedBuffer = ByteBuffer.allocate(len);
        decompressedBuffer = Request.doDecompress(compressedBuffer, len);
        for (int i = 0; i < len; i ++) {
            if (inputBuffer.array()[i] != decompressedBuffer.array()[i]) {
                Assert.fail("Fail at " + i);
            }
        }
    }
    
    public void testLogging() {
        s_logger.info("Testing Logging");
        GetHostStatsCommand cmd3 = new GetHostStatsCommand("hostguid", "hostname", 101);
        Request sreq = new Request(2, 3, new Command[] { cmd3 }, true, true);
        sreq.setSequence(1);
        Logger logger = Logger.getLogger(GsonHelper.class);
        Level level = logger.getLevel();

        logger.setLevel(Level.DEBUG);
        String log = sreq.log("Debug", true, Level.DEBUG);
        assert (log == null);

        log = sreq.log("Debug", false, Level.DEBUG);
        assert (log != null);

        logger.setLevel(Level.TRACE);
        log = sreq.log("Trace", true, Level.TRACE);
        assert (log.contains(GetHostStatsCommand.class.getSimpleName()));
        s_logger.debug(log);

        logger.setLevel(level);
    }

    protected void compareRequest(Request req1, Request req2) {
        assert req1.getSequence() == req2.getSequence();
        assert req1.getAgentId() == req2.getAgentId();
        assert req1.getManagementServerId() == req2.getManagementServerId();
        assert req1.isControl() == req2.isControl();
        assert req1.isFromServer() == req2.isFromServer();
        assert req1.executeInSequence() == req2.executeInSequence();
        assert req1.stopOnError() == req2.stopOnError();
        assert req1.getVersion().equals(req2.getVersion());
        assert req1.getViaAgentId() == req2.getViaAgentId();
        Command[] cmd1 = req1.getCommands();
        Command[] cmd2 = req2.getCommands();
        for (int i = 0; i < cmd1.length; i++) {
            assert cmd1[i].getClass().equals(cmd2[i].getClass());
        }
    }

}
