//
//  Copyright (C) 2001-2004 HorizonLive.com, Inc.  All Rights Reserved.
//  Copyright (C) 2001-2006 Constantin Kaplinsky.  All Rights Reserved.
//  Copyright (C) 2000 Tridia Corporation.  All Rights Reserved.
//  Copyright (C) 1999 AT&T Laboratories Cambridge.  All Rights Reserved.
//
//  This is free software; you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation; either version 2 of the License, or
//  (at your option) any later version.
//
//  This software is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with this software; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,
//  USA.
//

// repackage it to VMOps common packaging structure
package com.cloud.console;

//
// RfbProto.java
//

import java.io.*;
import java.awt.event.*;
import java.net.Socket;
import java.util.zip.*;

public class RfbProto {
	
  private static final Logger s_logger = Logger.getLogger(RfbProto.class);

  public final static String
    versionMsg_3_3 = "RFB 003.003\n",
    versionMsg_3_7 = "RFB 003.007\n",
    versionMsg_3_8 = "RFB 003.008\n";

  // Vendor signatures: standard VNC/RealVNC, TridiaVNC, and TightVNC
  public final static String
    StandardVendor  = "STDV",
    TridiaVncVendor = "TRDV",
    TightVncVendor  = "TGHT";

  // Security types
  public final static int
    SecTypeInvalid = 0,
    SecTypeNone    = 1,
    SecTypeVncAuth = 2,
    SecTypeTight   = 16;

  // Supported tunneling types
  public final static int
    NoTunneling = 0;
  public final static String
    SigNoTunneling = "NOTUNNEL";

  // Supported authentication types
  public final static int
    AuthNone      = 1,
    AuthVNC       = 2,
    AuthUnixLogin = 129;
  public final static String
    SigAuthNone      = "NOAUTH__",
    SigAuthVNC       = "VNCAUTH_",
    SigAuthUnixLogin = "ULGNAUTH";

  // VNC authentication results
  public final static int
    VncAuthOK      = 0,
    VncAuthFailed  = 1,
    VncAuthTooMany = 2;

  // Server-to-client messages
  public final static int
    FramebufferUpdate   = 0,
    SetColourMapEntries = 1,
    Bell                = 2,
    ServerCutText       = 3;

  // Client-to-server messages
  public final static int
    SetPixelFormat           = 0,
    FixColourMapEntries      = 1,
    SetEncodings             = 2,
    FramebufferUpdateRequest = 3,
    KeyboardEvent            = 4,
    PointerEvent             = 5,
    ClientCutText            = 6,
    VMOpsClientCustom        = 250;

  // Supported encodings and pseudo-encodings
  public final static int
    EncodingRaw            = 0,
    EncodingCopyRect       = 1,
    EncodingRRE            = 2,
    EncodingCoRRE          = 4,
    EncodingHextile        = 5,
    EncodingZlib           = 6,
    EncodingTight          = 7,
    EncodingZRLE           = 16,
    EncodingCompressLevel0 = 0xFFFFFF00,
    EncodingQualityLevel0  = 0xFFFFFFE0,
    EncodingXCursor        = 0xFFFFFF10,
    EncodingRichCursor     = 0xFFFFFF11,
    EncodingPointerPos     = 0xFFFFFF18,
    EncodingLastRect       = 0xFFFFFF20,
    EncodingNewFBSize      = 0xFFFFFF21;
  public final static String
    SigEncodingRaw            = "RAW_____",
    SigEncodingCopyRect       = "COPYRECT",
    SigEncodingRRE            = "RRE_____",
    SigEncodingCoRRE          = "CORRE___",
    SigEncodingHextile        = "HEXTILE_",
    SigEncodingZlib           = "ZLIB____",
    SigEncodingTight          = "TIGHT___",
    SigEncodingZRLE           = "ZRLE____",
    SigEncodingCompressLevel0 = "COMPRLVL",
    SigEncodingQualityLevel0  = "JPEGQLVL",
    SigEncodingXCursor        = "X11CURSR",
    SigEncodingRichCursor     = "RCHCURSR",
    SigEncodingPointerPos     = "POINTPOS",
    SigEncodingLastRect       = "LASTRECT",
    SigEncodingNewFBSize      = "NEWFBSIZ";

  public final static int MaxNormalEncoding = 255;

  // Contstants used in the Hextile decoder
  public final static int
    HextileRaw                 = 1,
    HextileBackgroundSpecified = 2,
    HextileForegroundSpecified = 4,
    HextileAnySubrects         = 8,
    HextileSubrectsColoured    = 16;

  // Contstants used in the Tight decoder
  public final static int TightMinToCompress = 12;
  public final static int
    TightExplicitFilter = 0x04,
    TightFill           = 0x08,
    TightJpeg           = 0x09,
    TightMaxSubencoding = 0x09,
    TightFilterCopy     = 0x00,
    TightFilterPalette  = 0x01,
    TightFilterGradient = 0x02;


  String host;
  int port;
  Socket sock;
  public SplitInputStream sis;
  public DataInputStream is;
  public OutputStream os;

  SessionRecorder rec;
  public boolean inNormalProtocol = false;
  
  RfbProtoAdapter adapter;
  // ConsoleViewer viewer;

  // Java on UNIX does not call keyPressed() on some keys, for example
  // swedish keys To prevent our workaround to produce duplicate
  // keypresses on JVMs that actually works, keep track of if
  // keyPressed() for a "broken" key was called or not. 
  boolean brokenKeyPressed = false;

  // This will be set to true on the first framebuffer update
  // containing Zlib-, ZRLE- or Tight-encoded data.
  boolean wereZlibUpdates = false;

  // This will be set to false if the startSession() was called after
  // we have received at least one Zlib-, ZRLE- or Tight-encoded
  // framebuffer update.
  boolean recordFromBeginning = true;

  // This fields are needed to show warnings about inefficiently saved
  // sessions only once per each saved session file.
  boolean zlibWarningShown;
  boolean tightWarningShown;

  // Before starting to record each saved session, we set this field
  // to 0, and increment on each framebuffer update. We don't flush
  // the SessionRecorder data into the file before the second update. 
  // This allows us to write initial framebuffer update with zero
  // timestamp, to let the player show initial desktop before
  // playback.
  int numUpdatesInSession;

  // Measuring network throughput.
  boolean timing;
  long timeWaitedIn100us;
  long timedKbits;

  // Protocol version and TightVNC-specific protocol options.
  public int serverMajor, serverMinor;
  public int clientMajor, clientMinor;
  boolean protocolTightVNC;
  CapsContainer tunnelCaps, authCaps;
  CapsContainer serverMsgCaps, clientMsgCaps;
  CapsContainer encodingCaps;

  // If true, informs that the RFB socket was closed.
  private boolean closed;
  
  //
  // Constructor. Make TCP connection to RFB server.
  //

  // RfbProto(String h, int p, ConsoleViewer v) throws IOException {
  public RfbProto(String h, int p, RfbProtoAdapter adapter) throws IOException {
		this.adapter = adapter;
		host = h;
		port = p;

/*
		if (!viewer.inProxyMode) {
*
			// Create a trust manager that does not validate certificate chains like the default TrustManager
			TrustManager[] trustAllCerts = new TrustManager[]{
					new X509TrustManager() {

						public java.security.cert.X509Certificate[] getAcceptedIssuers() {
							return null;
						}

						public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
							//No need to implement.
						}

						public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
							//No need to implement.
						}
					}
			};

			// Let us create the factory where we can set some parameters for the connection
			SSLContext sc = null;
			
			try {
				sc = SSLContext.getInstance("SSL");
				sc.init(null, trustAllCerts, new java.security.SecureRandom());
			} catch (Exception e) {
				throw new IOException(e);
			}

			SSLSocketFactory socketFactory =sc.getSocketFactory();
*
			
			SocketFactory socketFactory = SSLSocketFactory.getDefault();
	        sock = socketFactory.createSocket(host, port);
		} else {
			sock = new Socket();
			sock.setSoTimeout(0); // infinite
			sock.setKeepAlive(true);
			sock.connect(new InetSocketAddress(host, port), 30000);
		}
*/		
		sock = adapter.createConnection(h, p);
		
		sis = new SplitInputStream(new BufferedInputStream(sock.getInputStream(), 16384));
		is = new DataInputStream(sis);
		os = sock.getOutputStream();

		timing = false;
		timeWaitedIn100us = 5;
		timedKbits = 0;
	}

    public synchronized void close() {
		if (sock != null && !closed) {
			try {
				if(s_logger.isInfoEnabled())
					s_logger.info("close RFB socket");
			
				// set linger to zero and let us quit quickly 
				sock.setSoLinger(true, 0);
				sock.close();
			
				sock = null;
				closed = true;
				if (rec != null) {
					rec.close();
					rec = null;
				}
			} catch (Exception e) {
				if(s_logger.isInfoEnabled())
					s_logger.info("Exception in closing RFB socket, " + e.toString());
			
				sock = null;
				closed = true;
			
				if (rec != null) {
					try {
						rec.close();
					} catch (IOException e1) {
					}
					rec = null;
				}
			}
			
			if(s_logger.isInfoEnabled())
				s_logger.info("RFB socket is closed");
		}
	}

    public synchronized boolean closed() {
    	return closed;
    }

  //
  // Read proxy's protocol version message
  //

   public void readProxyVersion() throws Exception {
      byte[] version = new byte[12];
      readFully(version);
      if (version[0] != 'R' || version[1] != 'F' || version[2] != 'B') {
        throw new Exception("Bad version string from proxy");
      }
  }

  //
  // Write host:port to proxy
  //

   public void writeProxyString(String realhost, int realport, String sid) throws IOException {
    byte[] bs = (realhost + ":" + realport + ":" + sid + "\n").getBytes("US-ASCII");
//s_logger.info("writeProxyString " + realhost + ":" + realport );
    os.write('V');
    os.write('M');
    os.write(bs.length);
    os.write(bs);
  }

  //
  // Read server's protocol version message
  //

  public void readVersionMsg() throws Exception {

    byte[] b = new byte[12];

    readFully(b);

    if ((b[0] != 'R') || (b[1] != 'F') || (b[2] != 'B') || (b[3] != ' ')
	|| (b[4] < '0') || (b[4] > '9') || (b[5] < '0') || (b[5] > '9')
	|| (b[6] < '0') || (b[6] > '9') || (b[7] != '.')
	|| (b[8] < '0') || (b[8] > '9') || (b[9] < '0') || (b[9] > '9')
	|| (b[10] < '0') || (b[10] > '9') || (b[11] != '\n'))
    {
      throw new Exception("Host " + host + " port " + port +
			  " is not an RFB server");
    }

    serverMajor = (b[4] - '0') * 100 + (b[5] - '0') * 10 + (b[6] - '0');
    serverMinor = (b[8] - '0') * 100 + (b[9] - '0') * 10 + (b[10] - '0');

    if (serverMajor < 3) {
      throw new Exception("RFB server does not support protocol version 3");
    }
  }


  //
  // Write our protocol version message
  //

  public void writeVersionMsg() throws IOException {
    clientMajor = 3;
    if (serverMajor > 3 || serverMinor >= 8) {
      clientMinor = 8;
      os.write(versionMsg_3_8.getBytes());
    } else if (serverMinor >= 7) {
      clientMinor = 7;
      os.write(versionMsg_3_7.getBytes());
    } else {
      clientMinor = 3;
      os.write(versionMsg_3_3.getBytes());
    }
    protocolTightVNC = false;
  }


  //
  // Negotiate the authentication scheme.
  //

  public int negotiateSecurity() throws Exception {
    return (clientMinor >= 7) ?
      selectSecurityType() : readSecurityType();
  }

  //
  // Read security type from the server (protocol version 3.3).
  //

  public int readSecurityType() throws Exception {
    int secType = is.readInt();

    switch (secType) {
    case SecTypeInvalid:
      readConnFailedReason();
      return SecTypeInvalid;	// should never be executed
    case SecTypeNone:
    case SecTypeVncAuth:
      return secType;
    default:
      throw new Exception("Unknown security type from RFB server: " + secType);
    }
  }

  //
  // Select security type from the server's list (protocol versions 3.7/3.8).
  //

  public int selectSecurityType() throws Exception {
    int secType = SecTypeInvalid;

    // Read the list of secutiry types.
    int nSecTypes = is.readUnsignedByte();
    if (nSecTypes == 0) {
      readConnFailedReason();
      return SecTypeInvalid;	// should never be executed
    }
    byte[] secTypes = new byte[nSecTypes];
    readFully(secTypes);

    // Find out if the server supports TightVNC protocol extensions
    for (int i = 0; i < nSecTypes; i++) {
      if (secTypes[i] == SecTypeTight) {
	protocolTightVNC = true;
	os.write(SecTypeTight);
	return SecTypeTight;
      }
    }

    // Find first supported security type.
    for (int i = 0; i < nSecTypes; i++) {
      if (secTypes[i] == SecTypeNone || secTypes[i] == SecTypeVncAuth) {
	secType = secTypes[i];
	break;
      }
    }

    if (secType == SecTypeInvalid) {
      throw new Exception("Server did not offer supported security type");
    } else {
      os.write(secType);
    }

    return secType;
  }

  //
  // Perform "no authentication".
  //

  public void authenticateNone() throws Exception {
    if (clientMinor >= 8)
      readSecurityResult("No authentication");
  }

  //
  // Perform standard VNC Authentication.
  //

  public void authenticateVNC(String pw) throws Exception {
    byte[] challenge = new byte[16];
    readFully(challenge);

    if (pw.length() > 8)
      pw = pw.substring(0, 8);	// Truncate to 8 chars

    // Truncate password on the first zero byte.
    int firstZero = pw.indexOf(0);
    if (firstZero != -1)
      pw = pw.substring(0, firstZero);

    byte[] key = {0, 0, 0, 0, 0, 0, 0, 0};
    System.arraycopy(pw.getBytes(), 0, key, 0, pw.length());

    DesCipher des = new DesCipher(key);

    des.encrypt(challenge, 0, challenge, 0);
    des.encrypt(challenge, 8, challenge, 8);

    os.write(challenge);

    readSecurityResult("VNC authentication");
  }

  //
  // Read security result.
  // Throws an exception on authentication failure.
  //

  public void readSecurityResult(String authType) throws Exception {
    int securityResult = is.readInt();

    switch (securityResult) {
    case VncAuthOK:
//      s_logger.info(authType + ": success");
      break;
    case VncAuthFailed:
      if (clientMinor >= 8)
        readConnFailedReason();
      throw new AuthenticationException(authType + ": failed");
    case VncAuthTooMany:
      throw new AuthenticationException(authType + ": failed, too many tries");
    default:
      throw new AuthenticationException(authType + ": unknown result " + securityResult);
    }
  }

  //
  // Read the string describing the reason for a connection failure,
  // and throw an exception.
  //

  public void readConnFailedReason() throws Exception {
    int reasonLen = is.readInt();
    byte[] reason = new byte[reasonLen];
    readFully(reason);
    throw new AuthenticationException(new String(reason));
  }

  //
  // Initialize capability lists (TightVNC protocol extensions).
  //

  public void initCapabilities() {
    tunnelCaps    = new CapsContainer();
    authCaps      = new CapsContainer();
    serverMsgCaps = new CapsContainer();
    clientMsgCaps = new CapsContainer();
    encodingCaps  = new CapsContainer();

    // Supported authentication methods
    authCaps.add(AuthNone, StandardVendor, SigAuthNone,
		 "No authentication");
    authCaps.add(AuthVNC, StandardVendor, SigAuthVNC,
		 "Standard VNC password authentication");

    // Supported encoding types
    encodingCaps.add(EncodingCopyRect, StandardVendor,
		     SigEncodingCopyRect, "Standard CopyRect encoding");
    encodingCaps.add(EncodingRRE, StandardVendor,
		     SigEncodingRRE, "Standard RRE encoding");
    encodingCaps.add(EncodingCoRRE, StandardVendor,
		     SigEncodingCoRRE, "Standard CoRRE encoding");
    encodingCaps.add(EncodingHextile, StandardVendor,
		     SigEncodingHextile, "Standard Hextile encoding");
    encodingCaps.add(EncodingZRLE, StandardVendor,
		     SigEncodingZRLE, "Standard ZRLE encoding");
    encodingCaps.add(EncodingZlib, TridiaVncVendor,
		     SigEncodingZlib, "Zlib encoding");
    encodingCaps.add(EncodingTight, TightVncVendor,
		     SigEncodingTight, "Tight encoding");

    // Supported pseudo-encoding types
    encodingCaps.add(EncodingCompressLevel0, TightVncVendor,
		     SigEncodingCompressLevel0, "Compression level");
    encodingCaps.add(EncodingQualityLevel0, TightVncVendor,
		     SigEncodingQualityLevel0, "JPEG quality level");
    encodingCaps.add(EncodingXCursor, TightVncVendor,
		     SigEncodingXCursor, "X-style cursor shape update");
    encodingCaps.add(EncodingRichCursor, TightVncVendor,
		     SigEncodingRichCursor, "Rich-color cursor shape update");
    encodingCaps.add(EncodingPointerPos, TightVncVendor,
		     SigEncodingPointerPos, "Pointer position update");
    encodingCaps.add(EncodingLastRect, TightVncVendor,
		     SigEncodingLastRect, "LastRect protocol extension");
    encodingCaps.add(EncodingNewFBSize, TightVncVendor,
		     SigEncodingNewFBSize, "Framebuffer size change");
  }

  //
  // Setup tunneling (TightVNC protocol extensions)
  //

  public void setupTunneling() throws IOException {
    int nTunnelTypes = is.readInt();
    if (nTunnelTypes != 0) {
      readCapabilityList(tunnelCaps, nTunnelTypes);

      // We don't support tunneling yet.
      writeInt(NoTunneling);
    }
  }

  //
  // Negotiate authentication scheme (TightVNC protocol extensions)
  //

  public int negotiateAuthenticationTight() throws Exception {
    int nAuthTypes = is.readInt();
    if (nAuthTypes == 0)
      return AuthNone;

    readCapabilityList(authCaps, nAuthTypes);
    for (int i = 0; i < authCaps.numEnabled(); i++) {
      int authType = authCaps.getByOrder(i);
      if (authType == AuthNone || authType == AuthVNC) {
	writeInt(authType);
	return authType;
      }
    }
    throw new Exception("No suitable authentication scheme found");
  }

  //
  // Read a capability list (TightVNC protocol extensions)
  //

  public void readCapabilityList(CapsContainer caps, int count) throws IOException {
    int code;
    byte[] vendor = new byte[4];
    byte[] name = new byte[8];
    for (int i = 0; i < count; i++) {
      code = is.readInt();
      readFully(vendor);
      readFully(name);
      caps.enable(new CapabilityInfo(code, vendor, name));
    }
  }

  //
  // Write a 32-bit integer into the output stream.
  //

  public void writeInt(int value) throws IOException {
    byte[] b = new byte[4];
    b[0] = (byte) ((value >> 24) & 0xff);
    b[1] = (byte) ((value >> 16) & 0xff);
    b[2] = (byte) ((value >> 8) & 0xff);
    b[3] = (byte) (value & 0xff);
    os.write(b);
  }

  //
  // Write the client initialisation message
  //

  public void writeClientInit() throws IOException {
/*
    if (viewer.options.shareDesktop) {
      os.write(1);
    } else {
      os.write(0);
    }
*/
	  adapter.writeInit(os);
//    viewer.options.disableShareDesktop();
  }


  //
  // Read the server initialisation message
  //

  public String desktopName;
  public int framebufferWidth, framebufferHeight;
  public int bitsPerPixel, depth;
  public boolean bigEndian, trueColour;
  public int redMax, greenMax, blueMax, redShift, greenShift, blueShift;

  public void readServerInit() throws IOException {
    framebufferWidth = is.readUnsignedShort();
    framebufferHeight = is.readUnsignedShort();
    bitsPerPixel = is.readUnsignedByte();
    depth = is.readUnsignedByte();
    bigEndian = (is.readUnsignedByte() != 0);
    trueColour = (is.readUnsignedByte() != 0);
    redMax = is.readUnsignedShort();
    greenMax = is.readUnsignedShort();
    blueMax = is.readUnsignedShort();
    redShift = is.readUnsignedByte();
    greenShift = is.readUnsignedByte();
    blueShift = is.readUnsignedByte();
    byte[] pad = new byte[3];
    readFully(pad);
    int nameLength = is.readInt();
    byte[] name = new byte[nameLength];
    readFully(name);
    desktopName = new String(name);

    // Read interaction capabilities (TightVNC protocol extensions)
    if (protocolTightVNC) {
      int nServerMessageTypes = is.readUnsignedShort();
      int nClientMessageTypes = is.readUnsignedShort();
      int nEncodingTypes = is.readUnsignedShort();
      is.readUnsignedShort();
      readCapabilityList(serverMsgCaps, nServerMessageTypes);
      readCapabilityList(clientMsgCaps, nClientMessageTypes);
      readCapabilityList(encodingCaps, nEncodingTypes);
    }

    if(s_logger.isInfoEnabled())
	    s_logger.info("Recv'd ServerInit w=" + framebufferWidth + 
	    		" h=" + framebufferHeight + 
	    		" bits=" + bitsPerPixel +
	    		" depth=" + depth +
	    		" name=" + desktopName);

    inNormalProtocol = true;
  }


  //
  // Create session file and write initial protocol messages into it.
  //

  public void startSession(String fname) throws IOException {
    rec = new SessionRecorder(fname);
    rec.writeHeader();
    rec.write(versionMsg_3_3.getBytes());
    rec.writeIntBE(SecTypeNone);
    rec.writeShortBE(framebufferWidth);
    rec.writeShortBE(framebufferHeight);
    byte[] fbsServerInitMsg =	{
      32, 24, 0, 1, 0,
      (byte)0xFF, 0, (byte)0xFF, 0, (byte)0xFF,
      16, 8, 0, 0, 0, 0
    };
    rec.write(fbsServerInitMsg);
    rec.writeIntBE(desktopName.length());
    rec.write(desktopName.getBytes());
    numUpdatesInSession = 0;

    // FIXME: If there were e.g. ZRLE updates only, that should not
    //        affect recording of Zlib and Tight updates. So, actually
    //        we should maintain separate flags for Zlib, ZRLE and
    //        Tight, instead of one ``wereZlibUpdates'' variable.
    //
    if (wereZlibUpdates)
      recordFromBeginning = false;

    zlibWarningShown = false;
    tightWarningShown = false;
  }

  //
  // Close session file.
  //

  public void closeSession() throws IOException {
    if (rec != null) {
      rec.close();
      rec = null;
    }
  }


  //
  // Set new framebuffer size
  //

  public void setFramebufferSize(int width, int height) {
    framebufferWidth = width;
    framebufferHeight = height;
  }


  //
  // Read the server message type
  //

  public int readServerMessageType() throws IOException {
    int msgType = is.readUnsignedByte();

    // If the session is being recorded:
    if (rec != null) {
      if (msgType == Bell) {	// Save Bell messages in session files.
	rec.writeByte(msgType);
	if (numUpdatesInSession > 0)
	  rec.flush();
      }
    }

    return msgType;
  }


  //
  // Read a FramebufferUpdate message
  //

  int updateNRects;

  public void readFramebufferUpdate() throws IOException {
    is.readByte();
    updateNRects = is.readUnsignedShort();

    // If the session is being recorded:
    if (rec != null) {
      rec.writeByte(FramebufferUpdate);
      rec.writeByte(0);
      rec.writeShortBE(updateNRects);
    }

    numUpdatesInSession++;
  }

  // Read a FramebufferUpdate rectangle header

  int updateRectX, updateRectY, updateRectW, updateRectH, updateRectEncoding;

  public void readFramebufferUpdateRectHdr() throws Exception {
    updateRectX = is.readUnsignedShort();
    updateRectY = is.readUnsignedShort();
    updateRectW = is.readUnsignedShort();
    updateRectH = is.readUnsignedShort();
    updateRectEncoding = is.readInt();

    if (updateRectEncoding == EncodingZlib ||
        updateRectEncoding == EncodingZRLE ||
	updateRectEncoding == EncodingTight)
      wereZlibUpdates = true;

    // If the session is being recorded:
    if (rec != null) {
      if (numUpdatesInSession > 1)
	rec.flush();		// Flush the output on each rectangle.
      rec.writeShortBE(updateRectX);
      rec.writeShortBE(updateRectY);
      rec.writeShortBE(updateRectW);
      rec.writeShortBE(updateRectH);
      if (updateRectEncoding == EncodingZlib && !recordFromBeginning) {
	// Here we cannot write Zlib-encoded rectangles because the
	// decoder won't be able to reproduce zlib stream state.
	if (!zlibWarningShown) {
		if(s_logger.isInfoEnabled())
			s_logger.info("Warning: Raw encoding will be used " + "instead of Zlib in recorded session.");
		zlibWarningShown = true;
	}
	rec.writeIntBE(EncodingRaw);
      } else {
	rec.writeIntBE(updateRectEncoding);
	if (updateRectEncoding == EncodingTight && !recordFromBeginning &&
	    !tightWarningShown) {
		if(s_logger.isInfoEnabled())
		  s_logger.info("Warning: Re-compressing Tight-encoded " +
				     "updates for session recording.");
		tightWarningShown = true;
		}
      }
    }

    if (updateRectEncoding < 0 || updateRectEncoding > MaxNormalEncoding)
      return;

    if (updateRectX + updateRectW > framebufferWidth ||
	updateRectY + updateRectH > framebufferHeight) {
    	s_logger.warn("Framebuffer update rectangle too large: " +
			  updateRectW + "x" + updateRectH + " at (" +
			  updateRectX + "," + updateRectY + "), current framebuffer size:" + 
			  framebufferWidth + ", " + framebufferHeight);
    	
      throw new Exception("Framebuffer update rectangle too large: " +
			  updateRectW + "x" + updateRectH + " at (" +
			  updateRectX + "," + updateRectY + "), current framebuffer size:" + 
			  framebufferWidth + ", " + framebufferHeight);
    }
  }

  // Read CopyRect source X and Y.

  int copyRectSrcX, copyRectSrcY;

  public void readCopyRect() throws IOException {
    copyRectSrcX = is.readUnsignedShort();
    copyRectSrcY = is.readUnsignedShort();

    // If the session is being recorded:
    if (rec != null) {
      rec.writeShortBE(copyRectSrcX);
      rec.writeShortBE(copyRectSrcY);
    }
  }


  //
  // Read a ServerCutText message
  //

  public String readServerCutText() throws IOException {
    byte[] pad = new byte[3];
    readFully(pad);
    int len = is.readInt();
    byte[] text = new byte[len];
    readFully(text);
    return new String(text);
  }


  //
  // Read an integer in compact representation (1..3 bytes).
  // Such format is used as a part of the Tight encoding.
  // Also, this method records data if session recording is active and
  // the viewer's recordFromBeginning variable is set to true.
  //

  public int readCompactLen() throws IOException {
    int[] portion = new int[3];
    portion[0] = is.readUnsignedByte();
    int byteCount = 1;
    int len = portion[0] & 0x7F;
    if ((portion[0] & 0x80) != 0) {
      portion[1] = is.readUnsignedByte();
      byteCount++;
      len |= (portion[1] & 0x7F) << 7;
      if ((portion[1] & 0x80) != 0) {
	portion[2] = is.readUnsignedByte();
	byteCount++;
	len |= (portion[2] & 0xFF) << 14;
      }
    }

    if (rec != null && recordFromBeginning)
      for (int i = 0; i < byteCount; i++)
	rec.writeByte(portion[i]);

    return len;
  }

  //
  // Write a VMOps Client Custom message
  //

  public void writeClientCustomMessage(int type)
       throws IOException
  {
    byte[] b = new byte[2];

    b[0] = (byte) VMOpsClientCustom;
    b[1] = (byte) type;

    os.write(b);
  }

  //
  // Write a FramebufferUpdateRequest message
  //

  public void writeFramebufferUpdateRequest(int x, int y, int w, int h,
				     boolean incremental)
       throws IOException
  {
    byte[] b = new byte[10];

    b[0] = (byte) FramebufferUpdateRequest;
    b[1] = (byte) (incremental ? 1 : 0);
    b[2] = (byte) ((x >> 8) & 0xff);
    b[3] = (byte) (x & 0xff);
    b[4] = (byte) ((y >> 8) & 0xff);
    b[5] = (byte) (y & 0xff);
    b[6] = (byte) ((w >> 8) & 0xff);
    b[7] = (byte) (w & 0xff);
    b[8] = (byte) ((h >> 8) & 0xff);
    b[9] = (byte) (h & 0xff);

    os.write(b);
  }


  //
  // Write a SetPixelFormat message
  //

  public void writeSetPixelFormat(int bitsPerPixel, int depth, boolean bigEndian,
			   boolean trueColour,
			   int redMax, int greenMax, int blueMax,
			   int redShift, int greenShift, int blueShift)
       throws IOException
  {
    byte[] b = new byte[20];

    b[0]  = (byte) SetPixelFormat;
    b[4]  = (byte) bitsPerPixel;
    b[5]  = (byte) depth;
    b[6]  = (byte) (bigEndian ? 1 : 0);
    b[7]  = (byte) (trueColour ? 1 : 0);
    b[8]  = (byte) ((redMax >> 8) & 0xff);
    b[9]  = (byte) (redMax & 0xff);
    b[10] = (byte) ((greenMax >> 8) & 0xff);
    b[11] = (byte) (greenMax & 0xff);
    b[12] = (byte) ((blueMax >> 8) & 0xff);
    b[13] = (byte) (blueMax & 0xff);
    b[14] = (byte) redShift;
    b[15] = (byte) greenShift;
    b[16] = (byte) blueShift;

    os.write(b);
  }


  //
  // Write a FixColourMapEntries message.  The values in the red, green and
  // blue arrays are from 0 to 65535.
  //

  public void writeFixColourMapEntries(int firstColour, int nColours,
				int[] red, int[] green, int[] blue)
       throws IOException
  {
    byte[] b = new byte[6 + nColours * 6];

    b[0] = (byte) FixColourMapEntries;
    b[2] = (byte) ((firstColour >> 8) & 0xff);
    b[3] = (byte) (firstColour & 0xff);
    b[4] = (byte) ((nColours >> 8) & 0xff);
    b[5] = (byte) (nColours & 0xff);

    for (int i = 0; i < nColours; i++) {
      b[6 + i * 6]     = (byte) ((red[i] >> 8) & 0xff);
      b[6 + i * 6 + 1] = (byte) (red[i] & 0xff);
      b[6 + i * 6 + 2] = (byte) ((green[i] >> 8) & 0xff);
      b[6 + i * 6 + 3] = (byte) (green[i] & 0xff);
      b[6 + i * 6 + 4] = (byte) ((blue[i] >> 8) & 0xff);
      b[6 + i * 6 + 5] = (byte) (blue[i] & 0xff);
    }
 
    os.write(b);
  }


  //
  // Write a SetEncodings message
  //

  public void writeSetEncodings(int[] encs, int len) throws IOException {
    byte[] b = new byte[4 + 4 * len];

    b[0] = (byte) SetEncodings;
    b[2] = (byte) ((len >> 8) & 0xff);
    b[3] = (byte) (len & 0xff);

    for (int i = 0; i < len; i++) {
      b[4 + 4 * i] = (byte) ((encs[i] >> 24) & 0xff);
      b[5 + 4 * i] = (byte) ((encs[i] >> 16) & 0xff);
      b[6 + 4 * i] = (byte) ((encs[i] >> 8) & 0xff);
      b[7 + 4 * i] = (byte) (encs[i] & 0xff);
    }

    os.write(b);
  }


  //
  // Write a ClientCutText message
  //

  public void writeClientCutText(String text) throws IOException {
    byte[] b = new byte[8 + text.length()];

    b[0] = (byte) ClientCutText;
    b[4] = (byte) ((text.length() >> 24) & 0xff);
    b[5] = (byte) ((text.length() >> 16) & 0xff);
    b[6] = (byte) ((text.length() >> 8) & 0xff);
    b[7] = (byte) (text.length() & 0xff);

    System.arraycopy(text.getBytes(), 0, b, 8, text.length());

    os.write(b);
  }


  //
  // A buffer for putting pointer and keyboard events before being sent.  This
  // is to ensure that multiple RFB events generated from a single Java Event 
  // will all be sent in a single network packet.  The maximum possible
  // length is 4 modifier down events, a single key event followed by 4
  // modifier up events i.e. 9 key events or 72 bytes.
  //

  byte[] eventBuf = new byte[72];
  int eventBufLen;


  // Useful shortcuts for modifier masks.

  final static int CTRL_MASK  = InputEvent.CTRL_MASK;
  final static int SHIFT_MASK = InputEvent.SHIFT_MASK;
  final static int META_MASK  = InputEvent.META_MASK;
  final static int ALT_MASK   = InputEvent.ALT_MASK;


  //
  // Write a pointer event message.  We may need to send modifier key events
  // around it to set the correct modifier state.
  //

  int pointerMask = 0;

  public void writePointerEvent(MouseEvent evt) throws IOException {
    int modifiers = evt.getModifiers();

    int mask2 = 2;
    int mask3 = 4;
    Integer[] masks = new Integer[] { new Integer(mask2), new Integer(mask3) };
    adapter.swapMouseButton(masks);
    mask2 = masks[0];
    mask3 = masks[1];
    
    /*
    if (viewer.options.reverseMouseButtons2And3) {
      mask2 = 4;
      mask3 = 2;
    }
    */

    // Note: For some reason, AWT does not set BUTTON1_MASK on left
    // button presses. Here we think that it was the left button if
    // modifiers do not include BUTTON2_MASK or BUTTON3_MASK.

    if (evt.getID() == MouseEvent.MOUSE_PRESSED) {
      if ((modifiers & InputEvent.BUTTON2_MASK) != 0) {
        pointerMask = mask2;
        modifiers &= ~ALT_MASK;
      } else if ((modifiers & InputEvent.BUTTON3_MASK) != 0) {
        pointerMask = mask3;
        modifiers &= ~META_MASK;
      } else {
        pointerMask = 1;
      }
    } else if (evt.getID() == MouseEvent.MOUSE_RELEASED) {
      pointerMask = 0;
      if ((modifiers & InputEvent.BUTTON2_MASK) != 0) {
        modifiers &= ~ALT_MASK;
      } else if ((modifiers & InputEvent.BUTTON3_MASK) != 0) {
        modifiers &= ~META_MASK;
      }
    }

    eventBufLen = 0;
    writeModifierKeyEvents(modifiers);

    int x = evt.getX();
    int y = evt.getY();

    if (x < 0) x = 0;
    if (y < 0) y = 0;

    eventBuf[eventBufLen++] = (byte) PointerEvent;
    eventBuf[eventBufLen++] = (byte) pointerMask;
    eventBuf[eventBufLen++] = (byte) ((x >> 8) & 0xff);
    eventBuf[eventBufLen++] = (byte) (x & 0xff);
    eventBuf[eventBufLen++] = (byte) ((y >> 8) & 0xff);
    eventBuf[eventBufLen++] = (byte) (y & 0xff);

    //
    // Always release all modifiers after an "up" event
    //

    if (pointerMask == 0) {
      writeModifierKeyEvents(0);
    }

    os.write(eventBuf, 0, eventBufLen);
  }


  //
  // Write a key event message.  We may need to send modifier key events
  // around it to set the correct modifier state.  Also we need to translate
  // from the Java key values to the X keysym values used by the RFB protocol.
  //
  public void writeKeyEvent(KeyEvent evt) throws IOException {

    int keyChar = evt.getKeyChar();

    //
    // Ignore event if only modifiers were pressed.
    //

    // Some JVMs return 0 instead of CHAR_UNDEFINED in getKeyChar().
    if (keyChar == 0)
      keyChar = KeyEvent.CHAR_UNDEFINED;

    if (keyChar == KeyEvent.CHAR_UNDEFINED) {
      int code = evt.getKeyCode();
      if (code == KeyEvent.VK_CONTROL || code == KeyEvent.VK_SHIFT ||
          code == KeyEvent.VK_META || code == KeyEvent.VK_ALT)

    	  if(s_logger.isInfoEnabled())
    		  s_logger.info("Ignore sending of modifier key");
        return;
    }
    
    //
    // Key press or key release?
    //

    boolean down = (evt.getID() == KeyEvent.KEY_PRESSED);

    int key;
    if (evt.isActionKey()) {

      //
      // An action key should be one of the following.
      // If not then just ignore the event.
      //

      switch(evt.getKeyCode()) {
      case KeyEvent.VK_HOME:      key = 0xff50; break;
      case KeyEvent.VK_LEFT:      key = 0xff51; break;
      case KeyEvent.VK_UP:        key = 0xff52; break;
      case KeyEvent.VK_RIGHT:     key = 0xff53; break;
      case KeyEvent.VK_DOWN:      key = 0xff54; break;
      case KeyEvent.VK_PAGE_UP:   key = 0xff55; break;
      case KeyEvent.VK_PAGE_DOWN: key = 0xff56; break;
      case KeyEvent.VK_END:       key = 0xff57; break;
      case KeyEvent.VK_INSERT:    key = 0xff63; break;
      case KeyEvent.VK_F1:        key = 0xffbe; break;
      case KeyEvent.VK_F2:        key = 0xffbf; break;
      case KeyEvent.VK_F3:        key = 0xffc0; break;
      case KeyEvent.VK_F4:        key = 0xffc1; break;
      case KeyEvent.VK_F5:        key = 0xffc2; break;
      case KeyEvent.VK_F6:        key = 0xffc3; break;
      case KeyEvent.VK_F7:        key = 0xffc4; break;
      case KeyEvent.VK_F8:        key = 0xffc5; break;
      case KeyEvent.VK_F9:        key = 0xffc6; break;
      case KeyEvent.VK_F10:       key = 0xffc7; break;
      case KeyEvent.VK_F11:       key = 0xffc8; break;
      case KeyEvent.VK_F12:       key = 0xffc9; break;

      default:
		  if(s_logger.isInfoEnabled())
			  s_logger.info("Ignore sending of un-supprted action key, key code: " + evt.getKeyCode());
        return;
      }

    } else {

      //
      // A "normal" key press.  Ordinary ASCII characters go straight through.
      // For CTRL-<letter>, CTRL is sent separately so just send <letter>.
      // Backspace, tab, return, escape and delete have special keysyms.
      // Anything else we ignore.
      //

      key = keyChar;
      
      if (key < 0x20) {
    	  boolean converted = false;
          switch(key) {
          case KeyEvent.VK_BACK_SPACE: key = 0xff08; converted = true; break;
          case KeyEvent.VK_TAB:        key = 0xff09; converted = true; break;
          case KeyEvent.VK_ENTER:      key = 0xff0d; converted = true; break;
          case KeyEvent.VK_ESCAPE:     key = 0xff1b; converted = true; break;

          // For KVM QEMU
          case KeyEvent.VK_SHIFT:      key = 0xffe1; converted = true; break;
          }
          
          if(evt.isControlDown() && !converted) {
              if(key != KeyEvent.VK_CONTROL)
            	  key += 0x60;
          }
    	
    	/*
        if (evt.isControlDown()) {
          if(key != KeyEvent.VK_CONTROL && key != KeyEvent.VK_ESCAPE)
        	  key += 0x60;
          else if(key == KeyEvent.VK_ESCAPE)
        	  key = 0xff1b;
        } else {
          switch(key) {
          case KeyEvent.VK_BACK_SPACE: key = 0xff08; break;
          case KeyEvent.VK_TAB:        key = 0xff09; break;
          case KeyEvent.VK_ENTER:      key = 0xff0d; break;
          case KeyEvent.VK_ESCAPE:     key = 0xff1b; break;
          }
        }
        */
        
      } else if (key == 0x7f) {
    	  // Delete
    	  key = 0xffff;
      } else if (key > 0xff) {
    	  // JDK1.1 on X incorrectly passes some keysyms straight through,
    	  // so we do too.  JDK1.1.4 seems to have fixed this.
    	  // The keysyms passed are 0xff00 .. XK_BackSpace .. XK_Delete
    	  // Also, we pass through foreign currency keysyms (0x20a0..0x20af).
    	  if ((key < 0xff00 || key > 0xffff) &&
    			  !(key >= 0x20a0 && key <= 0x20af)) {
    		  
    		  if(s_logger.isInfoEnabled())
    			  s_logger.info("Ignore sending of un-supprted X keys, key : " + key);
    		  return;
    	  }
      }
    }

    // Fake keyPresses for keys that only generates keyRelease events
    if ((key == 0xe5) || (key == 0xc5) || // XK_aring / XK_Aring
    		(key == 0xe4) || (key == 0xc4) || // XK_adiaeresis / XK_Adiaeresis
    		(key == 0xf6) || (key == 0xd6) || // XK_odiaeresis / XK_Odiaeresis
    		(key == 0xa7) || (key == 0xbd) || // XK_section / XK_onehalf
    		(key == 0xa3)) {                  // XK_sterling
      // Make sure we do not send keypress events twice on platforms
      // with correct JVMs (those that actually report KeyPress for all
      // keys)	
      if (down)
    	  brokenKeyPressed = true;

      if (!down && !brokenKeyPressed) {
    	  // We've got a release event for this key, but haven't received
    	  // a press. Fake it. 
    	  eventBufLen = 0;
    	  writeModifierKeyEvents(evt.getModifiers());
    	  writeKeyEvent(key, true);
    	  os.write(eventBuf, 0, eventBufLen);
      }

      if (!down)
    	  brokenKeyPressed = false;  
    }
    
    // special need for KVM QEMU
    if(key == KeyEvent.VK_QUOTE)
    	key = 0x22;
    
    if(s_logger.isTraceEnabled())
    	s_logger.trace("Write key event, key: " + key + ", down: " + down + ", modifiers: " + evt.getModifiers());
    
    eventBufLen = 0;
    writeModifierKeyEvents(evt.getModifiers());
    writeKeyEvent(key, down);

    // Always release all modifiers after an "up" event
    if (!down)
      writeModifierKeyEvents(0);

    os.write(eventBuf, 0, eventBufLen);
  }
  


  //
  // Add a raw key event with the given X keysym to eventBuf.
  //

  public void writeKeyEvent(int keysym, boolean down) {
    eventBuf[eventBufLen++] = (byte) KeyboardEvent;
    eventBuf[eventBufLen++] = (byte) (down ? 1 : 0);
    eventBuf[eventBufLen++] = (byte) 0;
    eventBuf[eventBufLen++] = (byte) 0;
    eventBuf[eventBufLen++] = (byte) ((keysym >> 24) & 0xff);
    eventBuf[eventBufLen++] = (byte) ((keysym >> 16) & 0xff);
    eventBuf[eventBufLen++] = (byte) ((keysym >> 8) & 0xff);
    eventBuf[eventBufLen++] = (byte) (keysym & 0xff);
    
    if(s_logger.isTraceEnabled())
    	s_logger.trace("Send keyboard event on wire, keysym: 0x" + Integer.toHexString(keysym) + ", down: " + down);    
  }
  
  public void flushEventBuffer() throws IOException {
      os.write(eventBuf, 0, eventBufLen);
      eventBufLen = 0;
  }

  //
  // Write key events to set the correct modifier state.
  //
  int oldModifiers = 0;

  void writeModifierKeyEvents(int newModifiers) {
    if ((newModifiers & CTRL_MASK) != (oldModifiers & CTRL_MASK))
      writeKeyEvent(0xffe3, (newModifiers & CTRL_MASK) != 0);

    if ((newModifiers & SHIFT_MASK) != (oldModifiers & SHIFT_MASK))
      writeKeyEvent(0xffe1, (newModifiers & SHIFT_MASK) != 0);

    if ((newModifiers & META_MASK) != (oldModifiers & META_MASK))
      writeKeyEvent(0xffe7, (newModifiers & META_MASK) != 0);

    if ((newModifiers & ALT_MASK) != (oldModifiers & ALT_MASK))
      writeKeyEvent(0xffe9, (newModifiers & ALT_MASK) != 0);

    oldModifiers = newModifiers;
  }

  //
  // Compress and write the data into the recorded session file. This
  // method assumes the recording is on (rec != null).
  //
  public void recordCompressedData(byte[] data, int off, int len) throws IOException {
    Deflater deflater = new Deflater();
    deflater.setInput(data, off, len);
    int bufSize = len + len / 100 + 12;
    byte[] buf = new byte[bufSize];
    deflater.finish();
    int compressedSize = deflater.deflate(buf);
    recordCompactLen(compressedSize);
    rec.write(buf, 0, compressedSize);
  }

  public void recordCompressedData(byte[] data) throws IOException {
    recordCompressedData(data, 0, data.length);
  }

  //
  // Write an integer in compact representation (1..3 bytes) into the
  // recorded session file. This method assumes the recording is on
  // (rec != null).
  //

  public void recordCompactLen(int len) throws IOException {
    byte[] buf = new byte[3];
    int bytes = 0;
    buf[bytes++] = (byte)(len & 0x7F);
    if (len > 0x7F) {
      buf[bytes-1] |= 0x80;
      buf[bytes++] = (byte)(len >> 7 & 0x7F);
      if (len > 0x3FFF) {
	buf[bytes-1] |= 0x80;
	buf[bytes++] = (byte)(len >> 14 & 0xFF);
      }
    }
    rec.write(buf, 0, bytes);
  }

  public void startTiming() {
    timing = true;

    // Carry over up to 1s worth of previous rate for smoothing.

    if (timeWaitedIn100us > 10000) {
      timedKbits = timedKbits * 10000 / timeWaitedIn100us;
      timeWaitedIn100us = 10000;
    }
  }

  public void stopTiming() {
    timing = false; 
    if (timeWaitedIn100us < timedKbits/2)
      timeWaitedIn100us = timedKbits/2; // upper limit 20Mbit/s
  }

  public long kbitsPerSecond() {
    return timedKbits * 10000 / timeWaitedIn100us;
  }

  public long timeWaited() {
    return timeWaitedIn100us;
  }

  public void readFully(byte b[]) throws IOException {
    readFully(b, 0, b.length);
  }

  public void readFully(byte b[], int off, int len) throws IOException {
    long before = 0;
    if (timing)
      before = System.currentTimeMillis();

    is.readFully(b, off, len);

    if (timing) {
      long after = System.currentTimeMillis();
      long newTimeWaited = (after - before) * 10;
      int newKbits = len * 8 / 1000;

      // limit rate to between 10kbit/s and 40Mbit/s

      if (newTimeWaited > newKbits*1000) newTimeWaited = newKbits*1000;
      if (newTimeWaited < newKbits/4)    newTimeWaited = newKbits/4;

      timeWaitedIn100us += newTimeWaited;
      timedKbits += newKbits;
    }
  }
}
