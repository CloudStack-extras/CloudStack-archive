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

//
// AJAX console viewer
// Author
//		Kelven Yang
//
//		11/18/2009						Created
//
//		05/09/2011						Add keyboard type support
//
var g_logger;

/////////////////////////////////////////////////////////////////////////////
// class StringBuilder
//
function StringBuilder(initStr) {
    this.strings = new Array("");
    this.append(initStr);
}

StringBuilder.prototype = {
	append : function (str) {
	    if (str) {
	        this.strings.push(str);
	    }
	    return this;
	},
	
	clear : function() {
	    this.strings.length = 1;
	    return this;
	},
	
	toString: function() {
	    return this.strings.join("");
	}
};


function getCurrentLanguage() {
	if(acceptLanguages) {
		var tokens = acceptLanguages.split(',');
		if(tokens.length > 0)
			return tokens[0];
		
		return "en-us";
	} else {
		return "en-us";
	}
}

/////////////////////////////////////////////////////////////////////////////
// class KeyboardMapper
//
function KeyboardMapper(nativeKeypress, keyCodeMap, shiftedKeyCodeMap, charCodeMap, shiftedCharCodeMap) {
	this.nativeKeypress = nativeKeypress;
	this.keyCodeMap = keyCodeMap;
	this.shiftedKeyCodeMap = shiftedKeyCodeMap;
	this.charCodeMap = charCodeMap;
	this.shiftedCharCodeMap = shiftedCharCodeMap;
	
	this.mappedInput = [];
}

KeyboardMapper.prototype = {
	supportNativeKeypress : function() {
		return this.nativeKeypress;
	},
	
	inputFeed : function(eventType, code, modifiers) {
		if(this.nativeKeypress) {
			this.mappedInput.push({type: eventType, code: code, modifiers: modifiers});
			return;
		}
		
		if(code == AjaxViewer.SHIFT_KEY && eventType == AjaxViewer.KEY_DOWN)
			return;
		
		if(eventType == AjaxViewer.KEY_PRESS) {
			var mappedEntry = null;
			if((modifiers & AjaxViewer.SHIFT_KEY) != 0) {
				if(this.shiftedCharCodeMap && this.shiftedCharCodeMap[code])
					mappedEntry = this.shiftedCharCodeMap[code];
			} else {
				if(this.charCodeMap && this.charCodeMap[code])
					mappedEntry = this.charCodeMap[code];
			}

			// reset deferred charCode->keyCode resolution
			this.lastDeferedKeyCode = 0;
			if(mappedEntry) {
				if(mappedEntry.shift) {
					if(mappedEntry.keyCode) {
						this.mappedInput.push({type: AjaxViewer.KEY_DOWN, code: AjaxViewer.KEYCODE_SHIFT, modifiers: modifiers | AjaxViewer.SHIFT_KEY});
						this.mappedInput.push({type: AjaxViewer.KEY_DOWN, code: mappedEntry.keyCode, modifiers: modifiers | AjaxViewer.SHIFT_KEY});
						this.lastDeferedKeyCode = mappedEntry.keyCode;
					}
					this.mappedInput.push({type: eventType, code: mappedEntry.code, modifiers: modifiers | AjaxViewer.SHIFT_KEY});
					
					if((modifiers & AjaxViewer.SHIFT_KEY) != 0)
						g_logger.log(Logger.LEVEL_INFO, "map SHIFTED char code " + code + " to SHIFTED " +  mappedEntry.code + ", modifiers: " + modifiers + ', char: ' + String.fromCharCode(code));
					else
						g_logger.log(Logger.LEVEL_INFO, "map char code " + code + " to SHIFTED " +  mappedEntry.code + ", modifiers: " + modifiers + ', char: ' + String.fromCharCode(code));
				} else {
					if(mappedEntry.keyCode) {
						this.mappedInput.push({type: AjaxViewer.KEY_DOWN, code: mappedEntry.keyCode, modifiers: modifiers & ~AjaxViewer.SHIFT_KEY});
						this.lastDeferedKeyCode = mappedEntry.keyCode;
					}
					this.mappedInput.push({type: eventType, code: mappedEntry.code, modifiers: modifiers & ~AjaxViewer.SHIFT_KEY});
	
					if((modifiers & AjaxViewer.SHIFT_KEY) != 0)
						g_logger.log(Logger.LEVEL_INFO, "map SHIFTED char code " + code + " to " +  mappedEntry.code + ", modifiers: " + modifiers + ', char: ' + String.fromCharCode(code));
					else
						g_logger.log(Logger.LEVEL_INFO, "map char code " + code + " to " +  mappedEntry.code + ", modifiers: " + modifiers + ', char: ' + String.fromCharCode(code));
				}
			} else {
				if(code != 0)
					this.mappedInput.push({type: eventType, code: code, modifiers: modifiers});
			}
		} else {
			var mappedEntry = null;
			if((modifiers & AjaxViewer.SHIFT_KEY) != 0) {
				if(this.shiftedKeyCodeMap && this.shiftedKeyCodeMap[code])
					mappedEntry = this.shiftedKeyCodeMap[code];
			} else {
				if(this.keyCodeMap && this.keyCodeMap[code])
					mappedEntry = this.keyCodeMap[code];
			}
			
			if(mappedEntry) {
				if(mappedEntry.shift) {
					if(eventType == AjaxViewer.KEY_DOWN) {
						if(!mappedEntry.defer) {
							this.mappedInput.push({type: AjaxViewer.KEY_DOWN, code: AjaxViewer.KEYCODE_SHIFT, modifiers: modifiers | AjaxViewer.SHIFT_KEY});
							this.mappedInput.push({type: eventType, code: mappedEntry.code, modifiers: modifiers | AjaxViewer.SHIFT_KEY});
							
							if(mappedEntry.charCode)
								this.mappedInput.push({type: AjaxViewer.KEY_PRESS, code: mappedEntry.charCode, modifiers: modifiers | AjaxViewer.SHIFT_KEY});
						}
					} else {
						if(!mappedEntry.defer) {
							this.mappedInput.push({type: eventType, code: mappedEntry.code, modifiers: modifiers & ~AjaxViewer.SHIFT_KEY});
						} else {
							this.mappedInput.push({type: eventType, code: this.lastDeferedKeyCode, modifiers: modifiers & ~AjaxViewer.SHIFT_KEY});
						}
						
						this.mappedInput.push({type: AjaxViewer.KEY_UP, code: AjaxViewer.KEYCODE_SHIFT, modifiers: modifiers & ~AjaxViewer.SHIFT_KEY});
					}
					
					if((modifiers & AjaxViewer.SHIFT_KEY) != 0)
						g_logger.log(Logger.LEVEL_INFO, "map SHIFTED key code " + code + " to SHIFTED " +  mappedEntry.code + ", modifiers: " + modifiers + ', char: ' + String.fromCharCode(code));
					else
						g_logger.log(Logger.LEVEL_INFO, "map key code " + code + " to SHIFTED " +  mappedEntry.code + ", modifiers: " + modifiers + ', char: ' + String.fromCharCode(code));
				} else {
					if(eventType != AjaxViewer.KEY_DOWN || !mappedEntry.defer) {
						this.mappedInput.push({type: eventType, code: mappedEntry.code, modifiers: modifiers & ~AjaxViewer.SHIFT_KEY});
					}
						
					if((modifiers & AjaxViewer.SHIFT_KEY) != 0)
						g_logger.log(Logger.LEVEL_INFO, "map SHIFTED key code " + code + " to " +  mappedEntry.code + ", modifiers: " + modifiers + ', char: ' + String.fromCharCode(code));
					else
						g_logger.log(Logger.LEVEL_INFO, "map key code " + code + " to " +  mappedEntry.code + ", modifiers: " + modifiers + ', char: ' + String.fromCharCode(code));
				}
			} else {
				this.mappedInput.push({type: eventType, code: code, modifiers: modifiers});
			}
		}
	},
	
	getMappedInput : function() {
		var mappedInput = this.mappedInput;
		this.mappedInput = [];
		return mappedInput;
	},
	
	isModifierInput : function(code) {
		return $.inArray(code, [AjaxViewer.ALT_KEY, AjaxViewer.SHIFT_KEY, AjaxViewer.CTRL_KEY, AjaxViewer.META_KEY]) >= 0;
	}
};

/////////////////////////////////////////////////////////////////////////////
// class AjaxViewer
//
function AjaxViewer(panelId, imageUrl, updateUrl, tileMap, width, height, tileWidth, tileHeight, rawKeyboard, linuxGuest) {
	// logging is disabled by default so that it won't have negative impact on performance
	// however, a back door key-sequence can trigger to open the logger window, it is designed to help
	// trouble-shooting
	g_logger = new Logger();
	g_logger.enable(true);
	g_logger.open();
	g_logger.log(Logger.LEVEL_INFO, 'rawKeyboard: ' + rawKeyboard);
	
	var ajaxViewer = this;
	this.rawKeyboard = rawKeyboard;
	this.imageLoaded = false;
	this.fullImage = true;
	this.imgUrl = imageUrl;
	this.img = new Image();
	$(this.img).attr('src', imageUrl).load(function() {
		ajaxViewer.imageLoaded = true;
	});
	
	this.updateUrl = updateUrl;
	this.tileMap = tileMap;
	this.dirty = true;
	this.width = width;
	this.height = height;
	this.tileWidth = tileWidth;
	this.tileHeight = tileHeight;
	this.maxTileZIndex = 1;
	
	this.currentKeyboard = 0;
	this.keyboardMappers = [];
	
	this.linuxGuest = linuxGuest;
	
	this.timer = 0;
	this.eventQueue = [];
	this.sendingEventInProgress = false;
	
	this.lastClickEvent = { x: 0, y: 0, button: 0, modifiers: 0, time: new Date().getTime() };
	
	if(window.onStatusNotify == undefined)
		window.onStatusNotify = function(status) {};
	
	this.panel = this.generateCanvas(panelId, width, height, tileWidth, tileHeight);
	
	this.setupKeyCodeTranslationTable();
	this.setupKeyboardTranslationTable();
	this.setupUIController();
}

// client event types
AjaxViewer.MOUSE_MOVE = 1;
AjaxViewer.MOUSE_DOWN = 2;
AjaxViewer.MOUSE_UP = 3;
AjaxViewer.KEY_PRESS = 4;
AjaxViewer.KEY_DOWN = 5;
AjaxViewer.KEY_UP = 6;
AjaxViewer.EVENT_BAG = 7;
AjaxViewer.MOUSE_DBLCLK = 8;

// use java AWT key modifier masks 
AjaxViewer.SHIFT_KEY = 64;
AjaxViewer.CTRL_KEY = 128;
AjaxViewer.META_KEY = 256;
AjaxViewer.ALT_KEY = 512;
AjaxViewer.SHIFT_LEFT = 1024;
AjaxViewer.CTRL_LEFT = 2048;
AjaxViewer.ALT_LEFT = 4096;

// keycode
AjaxViewer.KEYCODE_SHIFT = 16;
AjaxViewer.KEYCODE_MULTIPLY = 106;
AjaxViewer.KEYCODE_ADD = 107;
AjaxViewer.KEYCODE_8 = 56;

AjaxViewer.CHARCODE_NUMPAD_MULTIPLY = 42;
AjaxViewer.CHARCODE_NUMPAD_ADD = 43;

AjaxViewer.EVENT_QUEUE_MOUSE_EVENT = 1;
AjaxViewer.EVENT_QUEUE_KEYBOARD_EVENT = 2;

AjaxViewer.STATUS_RECEIVING = 1;
AjaxViewer.STATUS_RECEIVED = 2;
AjaxViewer.STATUS_SENDING = 3;
AjaxViewer.STATUS_SENT = 4;

AjaxViewer.KEYBOARD_TYPE_ENGLISH = 0;
AjaxViewer.KEYBOARD_TYPE_JAPAN_EN_OS_TO_EN_VM = 1;
AjaxViewer.KEYBOARD_TYPE_JAPAN_JP_OS_TO_EN_VM = 2;
AjaxViewer.KEYBOARD_TYPE_JAPAN_EN_OS_TO_JP_VM = 3;
AjaxViewer.KEYBOARD_TYPE_JAPAN_JP_OS_TO_JP_VM = 4;

AjaxViewer.getEventName = function(type) {
	switch(type) {
	case AjaxViewer.MOUSE_MOVE :
		return "MOUSE_MOVE";
		
	case AjaxViewer.MOUSE_DOWN :
		return "MOUSE_DOWN";
		
	case AjaxViewer.MOUSE_UP :
		return "MOUSE_UP";
		
	case AjaxViewer.KEY_PRESS :
		return "KEY_PRESS";
		
	case AjaxViewer.KEY_DOWN :
		return "KEY_DOWN";
		
	case AjaxViewer.KEY_UP :
		return "KEY_UP";
		
	case AjaxViewer.EVENT_BAG :
		return "EVENT_BAG";
		
	case AjaxViewer.MOUSE_DBLCLK :
		return "MOUSE_DBLCLK";
	}
	
	return "N/A";
};

AjaxViewer.prototype = {
	setDirty: function(value) {
		this.dirty = value;
	},
	
	isDirty: function() {
		return this.dirty;
	},
	
	isImageLoaded: function() {
		return this.imageLoaded;
	},
	
	refresh: function(imageUrl, tileMap, fullImage) {
		var ajaxViewer = this;
		var img = $(this.img); 
		this.fullImage = fullImage;
		this.imgUrl=imageUrl;

		img.attr('src', imageUrl).load(function() {
			ajaxViewer.imageLoaded = true;
		});
		this.tileMap = tileMap;
	},
	
	resize: function(panelId, width, height, tileWidth, tileHeight) {
		$(".canvas_tile", document.body).each(function() {
			$(this).remove();
		});
		$("table", $("#" + panelId)).remove();
		
		this.width = width;
		this.height = height;
		this.tileWidth = tileWidth;
		this.tileHeight = tileHeight;
		this.panel = this.generateCanvas(panelId, width, height, tileWidth, tileHeight);
	},
	
	start: function() {
		var ajaxViewer = this;
		this.timer = setInterval(function() { ajaxViewer.heartbeat(); }, 50);
		
		$(document).bind("ajaxError", function(event, XMLHttpRequest, ajaxOptions, thrownError) {
			ajaxViewer.onAjaxError(event, XMLHttpRequest, ajaxOptions, thrownError);
		});
		
		this.eventQueue = [];	// reset event queue
		this.sendingEventInProgress = false;
		ajaxViewer.installMouseHook();
		ajaxViewer.installKeyboardHook();

		$(window).bind("resize", function() {
			ajaxViewer.onWindowResize();
		});
	},
	
	stop: function() {
		clearInterval(this.timer);
		this.deleteCanvas();

		this.uninstallMouseHook();
		this.uninstallKeyboardHook();	
		this.eventQueue = [];
		this.sendingEventInProgress = false;

		$(document).unbind("ajaxError");
		$(window).unbind("resize");
	},
	
	sendMouseEvent: function(event, x, y, whichButton, modifiers) {
		this.eventQueue.push({
			type: AjaxViewer.EVENT_QUEUE_MOUSE_EVENT,
			event: event,
			x: x,
			y: y,
			code: whichButton,
			modifiers: modifiers
		});
		this.checkEventQueue();
	},
	
	setupKeyCodeTranslationTable: function() {
		this.keyCodeMap = {};
		for(var i = 'a'.charCodeAt(); i < 'z'.charCodeAt(); i++)
			this.keyCodeMap[i] = { code: 65 + i - 'a'.charCodeAt(), shift: false };
		for(i = 'A'.charCodeAt(); i < 'Z'.charCodeAt(); i++)
			this.keyCodeMap[i] = { code: 65 + i - 'A'.charCodeAt(), shift: true };
		for(i = '0'.charCodeAt(); i < '9'.charCodeAt(); i++)
			this.keyCodeMap[i] = { code: 48 + i - '0'.charCodeAt(), shift: false };
		
		this.keyCodeMap['`'.charCodeAt()] = { code : 192, shift : false };
		this.keyCodeMap['~'.charCodeAt()] = { code : 192, shift : true };
		
		this.keyCodeMap[')'.charCodeAt()] = { code : 48, shift : true };
		this.keyCodeMap['!'.charCodeAt()] = { code : 49, shift : true };
		this.keyCodeMap['@'.charCodeAt()] = { code : 50, shift : true };
		this.keyCodeMap['#'.charCodeAt()] = { code : 51, shift : true };
		this.keyCodeMap['$'.charCodeAt()] = { code : 52, shift : true };
		this.keyCodeMap['%'.charCodeAt()] = { code : 53, shift : true };
		this.keyCodeMap['^'.charCodeAt()] = { code : 54, shift : true };
		this.keyCodeMap['&'.charCodeAt()] = { code : 55, shift : true };
		this.keyCodeMap['*'.charCodeAt()] = { code : 56, shift : true };
		this.keyCodeMap['('.charCodeAt()] = { code : 57, shift : true };
		
		this.keyCodeMap['-'.charCodeAt()] = { code : 109, shift : false };
		this.keyCodeMap['_'.charCodeAt()] = { code : 109, shift : true };
		this.keyCodeMap['='.charCodeAt()] = { code : 107, shift : false };
		this.keyCodeMap['+'.charCodeAt()] = { code : 107, shift : true };

		this.keyCodeMap['['.charCodeAt()] = { code : 219, shift : false };
		this.keyCodeMap['{'.charCodeAt()] = { code : 219, shift : true };
		this.keyCodeMap[']'.charCodeAt()] = { code : 221, shift : false };
		this.keyCodeMap['}'.charCodeAt()] = { code : 221, shift : true };
		this.keyCodeMap['\\'.charCodeAt()] = { code : 220, shift : false };
		this.keyCodeMap['|'.charCodeAt()] = { code : 220, shift : true };
		this.keyCodeMap[';'.charCodeAt()] = { code : 59, shift : false };
		this.keyCodeMap[':'.charCodeAt()] = { code : 59, shift : true };
		this.keyCodeMap['\''.charCodeAt()] = { code : 222 , shift : false };
		this.keyCodeMap['"'.charCodeAt()] = { code : 222, shift : true };
		this.keyCodeMap[','.charCodeAt()] = { code : 188 , shift : false };
		this.keyCodeMap['<'.charCodeAt()] = { code : 188, shift : true };
		this.keyCodeMap['.'.charCodeAt()] = { code : 190, shift : false };
		this.keyCodeMap['>'.charCodeAt()] = { code : 190, shift : true };
		this.keyCodeMap['/'.charCodeAt()] = { code : 191, shift : false };
		this.keyCodeMap['?'.charCodeAt()] = { code : 191, shift : true };
	},
	
	setupKeyboardTranslationTable : function() {
		this.keyboardMappers = [];
		this.keyboardMappers[AjaxViewer.KEYBOARD_TYPE_ENGLISH] = new KeyboardMapper(true, null, null, null, null);
		this.setJapaneseKeyboardOnEnglishOsToEnglishVmMapping();
		this.setJapaneseKeyboardOnJapaneseOsToEnglishVmMapping();
		this.setJapaneseKeyboardOnEnglishOsToJapaneseVmMapping();
		this.setJapaneseKeyboardOnJapaneseOsToJapaneseVmMapping();
	},
	
	setJapaneseKeyboardOnEnglishOsToEnglishVmMapping : function () {
		var keyCodeMap = [];
		var shiftedKeyCodeMap = [];
		var charCodeMap = [];
		var shiftedCharCodeMap = [];
		
		shiftedKeyCodeMap[50] 	= { code: 222, shift: 1 } ;						// JP SHIFT + 2 -> "
		shiftedCharCodeMap[64] 	= { code: 34, shift: 1 };
		
		shiftedKeyCodeMap[54] = { code: 55, shift : 1 };						// JP SHIFT + 6 -> &
		shiftedCharCodeMap[94] = { code: 38, shift : 1 };
		
		shiftedKeyCodeMap[55] = { code: 222, shift : 0 };						// JP SHIFT + 7 -> '
		shiftedCharCodeMap[38] = { code: 39, shift : 0 };
		
		shiftedKeyCodeMap[56] = { code: 57, shift : 1 };						// JP SHIFT + 8 -> (
		shiftedCharCodeMap[42] = { code: 40, shift : 1 };
		
		shiftedKeyCodeMap[57] = { code: 48, shift : 1 };						// JP SHIFT + 9 -> )
		shiftedCharCodeMap[40] = { code: 41, shift : 1 };

		shiftedKeyCodeMap[48] = { code: 192, shift : 1 };						// JP SHIFT + 0 -> ~
		shiftedCharCodeMap[41] = { code: 126, shift : 1 };

		shiftedKeyCodeMap[109] = { code: 107, shift : 0 };						// JP SHIFT + (-=), keycode/charcode(109, 95) from Firefox
		shiftedCharCodeMap[95] = { code: 61, shift : 0 };
		
		shiftedKeyCodeMap[189] = { code: 107, shift : 0 };						// JP SHIFT + (-=), keycode/charcode(109, 95) from Chrome/Safari/MSIE
		shiftedCharCodeMap[95] = { code: 61, shift : 0 };

		if($.browser.mozilla) {
			keyCodeMap[107] = { code: 107, shift : 1, defer : true };			// JP NUM +, keycode/charcode (107, 43) from Firefox
			charCodeMap[43] = { code: 43, shift : 1, keyCode: 107 };
			charCodeMap[61] = { code: 94, shift : 1, keyCode: 54 };				// JP (~^), keycode/charcode (107, 61) from Firefox
			
			shiftedKeyCodeMap[107] = { code: 192, shift : 1 };					// JP SHIFT + (!^)				
			shiftedCharCodeMap[43] = { code: 126, shift : 1 };
		} else {
			keyCodeMap[187] = { code: 54, shift: 1};							// JP ~^
			charCodeMap[61] = { code: 94, shift: 1};
			
			shiftedKeyCodeMap[187] = { code: 192, shift : 1 };					// JP SHIFT + (~^)				
			shiftedCharCodeMap[43] = { code: 126, shift : 1 };
		}
		
		shiftedKeyCodeMap[255] = { code: 220, shift : 1, charCode: 124 };		// JP (|-, key before backspace), Japanese Yen mark
		
		keyCodeMap[219] = { code: 50, shift : 1 };								// JP @`
		charCodeMap[91] = { code: 64, shift : 1 };
		shiftedKeyCodeMap[219] = { code: 192, shift : 0 };						// JP SHIFT + (@`)
		shiftedCharCodeMap[123] = { code: 96, shift : 0 };
		
		keyCodeMap[221] = { code: 219, shift : 0 };								// JP [{
		charCodeMap[93] = { code: 91, shift : 0 };
		shiftedKeyCodeMap[221] = { code: 219, shift : 1 };
		shiftedCharCodeMap[125] = { code: 123, shift : 1 };

		if($.browser.mozilla) {
			shiftedKeyCodeMap[59] = { code: 107, shift : 1 };					// JP ;+
			shiftedCharCodeMap[58] = { code: 43, shift : 1 };
		} else {
			shiftedKeyCodeMap[186] = { code: 107, shift : 1 };					// JP ;+
			shiftedCharCodeMap[58] = { code: 43, shift : 1 };
		}
		
		keyCodeMap[222] = { code: 59, shift : 1 };								// JP :*
		charCodeMap[39] = { code: 58, shift : 1 };
		shiftedKeyCodeMap[222] = { code: 56, shift : 1 };
		shiftedCharCodeMap[34] = { code: 42, shift : 1 };
		
		keyCodeMap[220] = { code: 221, shift : 0 };								// JP ]}
		charCodeMap[92] = { code: 93, shift : 0 };
		shiftedKeyCodeMap[220] = { code: 221, shift : 1 };
		shiftedCharCodeMap[124] = { code: 125, shift : 1 };
		
		keyCodeMap[193] = { code: 220, shift : 0, charCode: 92 };				// JP \_
		shiftedKeyCodeMap[193] = { code: 109, shift : 1, charCode: 95 };
		
		keyCodeMap[106] = { code: 56, shift : 1 };								// JP NUM *
		charCodeMap[42] = { code: 42, shift : 1 };
		
		keyCodeMap[110] = { code: 190, shift : 0 };								// JP NUM .
		charCodeMap[46] = { code: 46, shift : 0 };
		this.keyboardMappers[AjaxViewer.KEYBOARD_TYPE_JAPAN_EN_OS_TO_EN_VM] = new KeyboardMapper(false, keyCodeMap, shiftedKeyCodeMap, 
			charCodeMap, shiftedCharCodeMap);
	},
	
	setJapaneseKeyboardOnJapaneseOsToEnglishVmMapping : function () {
		var keyCodeMap = [];
		var shiftedKeyCodeMap = [];
		var charCodeMap = [];
		var shiftedCharCodeMap = [];
		
		shiftedKeyCodeMap[50] 	= { code: 222, shift: 1, defer: true };			// JP SHIFT + 2 -> "
		shiftedCharCodeMap[34] = { code: 34, shift : 1, keyCode: 222 };
		
		shiftedKeyCodeMap[54] = { code: 55, shift : 1 };						// JP SHIFT + 6 -> &
		
		shiftedKeyCodeMap[55] = { code: 222, shift : 0 };						// JP SHIFT + 7 -> '
		shiftedCharCodeMap[39] = { code: 39, shift : 0 };
		
		shiftedKeyCodeMap[56] = { code: 57, shift : 1 };						// JP SHIFT + 8 -> (
		shiftedCharCodeMap[42] = { code: 40, shift : 1 };
		
		shiftedKeyCodeMap[57] = { code: 48, shift : 1 };						// JP SHIFT + 9 -> )
		shiftedCharCodeMap[40] = { code: 41, shift : 1 };

		shiftedKeyCodeMap[48] = { code: 192, shift : 1 };						// JP SHIFT + 0 -> ~
		shiftedCharCodeMap[41] = { code: 126, shift : 1 };

		shiftedKeyCodeMap[109] = { code: 107, shift : 0 };						// JP SHIFT + (-=), keycode/charcode(109, 95) from Firefox
		shiftedCharCodeMap[95] = { code: 61, shift : 0 };
		
		shiftedKeyCodeMap[189] = { code: 107, shift : 0 };						// JP SHIFT + (-=), keycode/charcode(109, 95) from Chrome/Safari/MSIE
		shiftedCharCodeMap[95] = { code: 61, shift : 0 };

		keyCodeMap[222] = { code: 54, shift: 1};								// JP ~^
		charCodeMap[94] = { code: 94, shift: 1};
		
		shiftedKeyCodeMap[222] = { code: 192, shift : 1 };						// JP SHIFT + (~^)				
		shiftedCharCodeMap[126] = { code: 126, shift : 1 };
		
		shiftedKeyCodeMap[220] = { code: 220, shift : 1, charCode: 124 };		// JP (|-, key before backspace)
		
		keyCodeMap[192] = { code: 50, shift : 1 };								// JP @`
		charCodeMap[64] = { code: 64, shift : 1 };
		shiftedKeyCodeMap[192] = { code: 192, shift : 0 };						// JP SHIFT + (@`)
		shiftedCharCodeMap[96] = { code: 96, shift : 0 };

		if($.browser.mozilla) {
			keyCodeMap[107] = { code: 59, shift : 0 };							// JP ;+
			charCodeMap[59] = { code: 59, shift : 0 };
		} else {
			keyCodeMap[187] = { code: 59, shift : 0 };							// JP ;+
			charCodeMap[59] = { code: 59, shift : 0 };
		}
		
		if($.browser.mozilla) {
			keyCodeMap[59] = { code: 59, shift : 1 };							// JP :*
			charCodeMap[58] = { code: 58, shift : 1 };
			shiftedKeyCodeMap[59] = { code: 56, shift : 1 };
			shiftedCharCodeMap[42] = { code: 42, shift : 1 };
		} else {
			keyCodeMap[186] = { code: 59, shift : 1 };							// JP :*
			charCodeMap[58] = { code: 58, shift : 1 };
			shiftedKeyCodeMap[186] = { code: 56, shift : 1 };
			shiftedCharCodeMap[42] = { code: 42, shift : 1 };
		}
		
		keyCodeMap[226] = { code: 220, shift : 0 };								// JP \_
		shiftedKeyCodeMap[226] = { code: 109, shift : 1 };
		
		keyCodeMap[106] = { code: 56, shift : 1 };								// JP NUM *
		charCodeMap[42] = { code: 42, shift : 1 };
		
		keyCodeMap[110] = { code: 190, shift : 0 };								// JP NUM .
		charCodeMap[46] = { code: 46, shift : 0 };
		 
		this.keyboardMappers[AjaxViewer.KEYBOARD_TYPE_JAPAN_JP_OS_TO_EN_VM] = new KeyboardMapper(false, keyCodeMap, shiftedKeyCodeMap, 
			charCodeMap, shiftedCharCodeMap);
	},
	
	setJapaneseKeyboardOnEnglishOsToJapaneseVmMapping : function () {
		
		var keyCodeMap = [];
		var shiftedKeyCodeMap = [];
		var charCodeMap = [];
		var shiftedCharCodeMap = [];
		
		if(this.linuxGuest) {
			// for LINUX guest OSes
			
			shiftedKeyCodeMap[50] 	= { code: 222, shift: 1 } ;						// JP SHIFT + 2 -> "
			shiftedCharCodeMap[64] 	= { code: 34, shift: 1 };
			
			shiftedKeyCodeMap[54] = { code: 55, shift : 1 };						// JP SHIFT + 6 -> &
			shiftedCharCodeMap[94] = { code: 38, shift : 1 };
			
			shiftedKeyCodeMap[55] = { code: 222, shift : 0 };						// JP SHIFT + 7 -> '
			shiftedCharCodeMap[38] = { code: 39, shift : 1 };
			
			shiftedKeyCodeMap[56] = { code: 57, shift : 1 };						// JP SHIFT + 8 -> (
			shiftedCharCodeMap[42] = { code: 40, shift : 1 };
			
			shiftedKeyCodeMap[57] = { code: 48, shift : 1 };						// JP SHIFT + 9 -> )
			shiftedCharCodeMap[40] = { code: 41, shift : 1 };

			shiftedKeyCodeMap[48] = { code: 192, shift : 1 };						// JP SHIFT + 0 -> ~
			shiftedCharCodeMap[41] = { code: 126, shift : 1 };

			shiftedKeyCodeMap[109] = { code: 107, shift : 1 };						// JP SHIFT + (-=), keycode/charcode(109, 95) from Firefox
			shiftedCharCodeMap[95] = { code: 61, shift : 0 };
			
			shiftedKeyCodeMap[189] = { code: 107, shift : 1 };						// JP SHIFT + (-=), keycode/charcode(109, 95) from Chrome/Safari/MSIE
			shiftedCharCodeMap[95] = { code: 61, shift : 0 };

			shiftedKeyCodeMap[222] = { code: 192, shift : 1 };						// JP SHIFT + (~^)				
			shiftedCharCodeMap[126] = { code: 126, shift : 1 };
			
			if($.browser.mozilla) {
				keyCodeMap[107] = { code: 107, shift : 1, defer : true };			// JP NUM +, keycode/charcode (107, 43) from Firefox
				charCodeMap[43] = { code: 43, shift : 1, keyCode: 43 };
				charCodeMap[61] = { code: 94, shift : 0, keyCode: 94 };				// JP (~^), keycode/charcode (107, 61) from Firefox
				
				shiftedKeyCodeMap[107] = { code: 192, shift : 1 };					// JP SHIFT + (!^)				
				shiftedCharCodeMap[43] = { code: 126, shift : 1 };
			} else {
				keyCodeMap[187] = { code: 54, shift: 1, defer: true };				// JP ~^
				charCodeMap[61] = { code: 94, shift: 0, keyCode: 94 };
				
				shiftedKeyCodeMap[187] = { code: 192, shift : 1 };					// JP SHIFT + (~^)
				shiftedCharCodeMap[43] = { code: 126, shift : 1 };
				
				keyCodeMap[107] = { code: 107, shift : 0, defer: true };			// JP NUM +, keycode/charcode(107, 43)				
				charCodeMap[43] = { code: 43, shift : 1, keyCode: 43 };
			}
			
			shiftedKeyCodeMap[255] = { code: 220, shift : 1, charCode: 124 };		// JP (|-, key before backspace), Japanese Yen mark

			keyCodeMap[219] = { code: 192, shift : 0 };								// JP @`
			charCodeMap[91] = { code: 96, shift : 0 };
			shiftedKeyCodeMap[219] = { code: 50, shift : 1 };						// JP SHIFT + (@`)
			shiftedCharCodeMap[123] = { code: 64, shift : 1 };
			
			keyCodeMap[221] = { code: 219, shift : 0 };								// JP [{
			charCodeMap[93] = { code: 91, shift : 0 };
			shiftedKeyCodeMap[221] = { code: 219, shift : 1 };
			shiftedCharCodeMap[125] = { code: 123, shift : 1 };

			if($.browser.mozilla) {
				shiftedKeyCodeMap[59] = { code: 107, shift : 1, defer: true };		// JP ;+
				shiftedCharCodeMap[58] = { code: 43, shift : 1, keyCode: 43 };
			} else {
				shiftedKeyCodeMap[186] = { code: 107, shift : 1, defer: true };		// JP ;+
				shiftedCharCodeMap[58] = { code: 43, shift : 1, keyCode: 43 };
			}
			
			keyCodeMap[222] = { code: 59, shift : 0, defer : true };				// JP :*
			charCodeMap[39] = { code: 59, shift : 0, keyCode: 58 };
			shiftedKeyCodeMap[222] = { code: 56, shift : 1 };
			shiftedCharCodeMap[34] = { code: 42, shift : 1 };
			
			keyCodeMap[220] = { code: 221, shift : 0 };								// JP ]}
			charCodeMap[92] = { code: 93, shift : 0 };
			shiftedKeyCodeMap[220] = { code: 221, shift : 1 };
			shiftedCharCodeMap[124] = { code: 125, shift : 1 };

			keyCodeMap[106] = { code: 222, shift : 1, defer: true };				// JP NUM *
			charCodeMap[42] = { code: 42, shift : 1, keyCode: 42 };
			
			keyCodeMap[110] = { code: 190, shift : 0 };								// JP NUM .
			charCodeMap[46] = { code: 46, shift : 0 };
	
			keyCodeMap[193] = { code: 220, shift : 0, charCode: 92 };				// JP key left to right shift on JP keyboard
			shiftedKeyCodeMap[193] = { code: 189, shift: 1, charCode: 64 };
			
			keyCodeMap[255] = { code: 220, shift : 0, charCode: 92 };				// JP Japanese Yen mark on JP keyboard
			shiftedKeyCodeMap[255] = { code: 220, shift: 1, charCode: 95 };
			
		} else {
			// for windows guest OSes
			keyCodeMap[106] = { code: 222, shift : 1 };								// JP NUM *
			charCodeMap[42] = { code: 34, shift : 1 };
			
			keyCodeMap[110] = { code: 190, shift : 0 };								// JP NUM .
			charCodeMap[46] = { code: 46, shift : 0 };
	
			keyCodeMap[193] = { code: 220, shift : 0, charCode: 92 };				// JP key left to right shift on JP keyboard
			shiftedKeyCodeMap[193] = { code: 189, shift: 1, charCode: 64 };
			
			keyCodeMap[255] = { code: 220, shift : 0, charCode: 92 };				// JP Japanese Yen mark on JP keyboard
			shiftedKeyCodeMap[255] = { code: 220, shift: 1, charCode: 95 };
		}
		this.keyboardMappers[AjaxViewer.KEYBOARD_TYPE_JAPAN_EN_OS_TO_JP_VM] = new KeyboardMapper(false, keyCodeMap, shiftedKeyCodeMap, 
				charCodeMap, shiftedCharCodeMap);
	},
	
	setJapaneseKeyboardOnJapaneseOsToJapaneseVmMapping : function () {
		var keyCodeMap = [];
		var shiftedKeyCodeMap = [];
		var charCodeMap = [];
		var shiftedCharCodeMap = [];
		
		if(this.linuxGuest) {
			shiftedKeyCodeMap[50] 	= { code: 50, shift: 1, defer: true };			// JP SHIFT + 2 -> "
			shiftedCharCodeMap[34] = { code: 34, shift : 1, keyCode: 34 };
			
			shiftedKeyCodeMap[54] = { code: 55, shift : 1 };						// JP SHIFT + 6 -> &
			shiftedCharCodeMap[94] = { code: 38, shift : 1 };

			shiftedKeyCodeMap[55] = { code: 222, shift : 0, defer:true };			// JP SHIFT + 7 -> '
			shiftedCharCodeMap[39] = { code: 39, shift : 1, keyCode: 39 };
			
			shiftedKeyCodeMap[56] = { code: 57, shift : 1 };						// JP SHIFT + 8 -> (
			shiftedCharCodeMap[42] = { code: 40, shift : 1 };
			
			shiftedKeyCodeMap[57] = { code: 48, shift : 1 };						// JP SHIFT + 9 -> )
			shiftedCharCodeMap[40] = { code: 41, shift : 1 };

			shiftedKeyCodeMap[48] = { code: 192, shift : 1 };						// JP SHIFT + 0 -> ~
			shiftedCharCodeMap[41] = { code: 126, shift : 1 };

			keyCodeMap[222] = { code: 107, shift: 0, defer: true };					// JP ~^
			charCodeMap[94] = { code: 94, shift: 0, keyCode: 94 };
			shiftedKeyCodeMap[222] = { code: 192, shift : 1, defer: true };			// JP SHIFT + (~^)				
			shiftedCharCodeMap[126] = { code: 126, shift : 1 };

			shiftedKeyCodeMap[192] = { code: 50, shift : 1 };						// JP SHIFT + (@`)
			shiftedCharCodeMap[96] = { code: 64, shift : 1 };
			
			if($.browser.mozilla) {
				shiftedKeyCodeMap[109] = { code: 107, shift : 1 };					// JP SHIFT + (-=), keycode/charcode(109, 95) from Firefox

				// Note, keycode 107 is duplicated with "+" key at NUM pad
				keyCodeMap[107] = { code: 59, shift : 0, defer: true };				// JP ;+
				charCodeMap[59] = { code: 58, shift : 0, keyCode: 59 };
				charCodeMap[43] = { code: 43, shift : 1, keyCode: 43 };				// JP NUM +
				
				shiftedKeyCodeMap[107] = { code: 59, shift : 0, defer: true };		// JP ;+
				shiftedCharCodeMap[43] = { code: 43, shift : 1, keyCode: 43 };

				keyCodeMap[59] = { code: 59, shift : 0, defer : true };				// JP :*
				charCodeMap[58] = { code: 58, shift : 0, keyCode: 58 };
			} else {
				shiftedKeyCodeMap[189] = { code: 107, shift : 1 };					// JP SHIFT + (-=), keycode/charcode(109, 95) from Chrome/Safari/MSIE
				shiftedCharCodeMap[95] = { code: 61, shift : 0 };
				
				keyCodeMap[187] = { code: 59, shift : 0, defer: true };				// JP ;+
				charCodeMap[59] = { code: 58, shift : 0, keyCode: 59 };
				shiftedKeyCodeMap[187] = { code: 59, shift : 1, defer: true };
				shiftedCharCodeMap[43] = { code: 43, shift : 1, keyCode: 43 };
	
				keyCodeMap[107] = { code: 59, shift : 0, defer: true };							// JP NUM +
				charCodeMap[43] = { code: 43, shift : 1, keyCode: 43};

				keyCodeMap[186] = { code: 59, shift : 0, defer: true };				// JP :*
				charCodeMap[58] = { code: 58, shift : 0, keyCode: 58 };
			}

			keyCodeMap[226] = { code: 220, shift : 0, charCode: 92 };				// JP key left to right shift on JP keyboard
			shiftedKeyCodeMap[226] = { code: 189, shift: 1 };
			
		} else {
			// windows guest
			shiftedKeyCodeMap[50] 	= { code: 50, shift: 1, defer: true };			// JP SHIFT + 2 -> "
			shiftedCharCodeMap[34] = { code: 0, shift : 1, keyCode: 50 };
	
			shiftedKeyCodeMap[55] = { code: 222, shift : 0, defer:true };			// JP SHIFT + 7 -> '
			shiftedCharCodeMap[39] = { code: 0, shift : 1, keyCode: 55 };
			
			keyCodeMap[222] = { code: 107, shift: 0 };								// JP ~^
			charCodeMap[94] = { code: 59, shift: 0 };
			
			shiftedKeyCodeMap[222] = { code: 107, shift : 1 };						// JP SHIFT + (~^)				
			shiftedCharCodeMap[126] = { code: 43, shift : 1 };
	
			keyCodeMap[192] = { code: 219, shift : 0 };								// JP @`
			charCodeMap[64] = { code: 91, shift : 0 };
			shiftedKeyCodeMap[192] = { code: 219, shift : 1 };						// JP SHIFT + (@`)
			shiftedCharCodeMap[96] = { code: 123, shift : 1 };
	
			keyCodeMap[219] = { code: 221, shift : 0 };								// JP [{
			charCodeMap[91] = { code: 93, shift : 0 };
			shiftedKeyCodeMap[219] = { code: 221, shift : 1 };
			shiftedCharCodeMap[123] = { code: 125, shift : 1 };
			
			if($.browser.mozilla) {
				// Note, keycode 107 is duplicated with "+" key at NUM pad
				keyCodeMap[107] = { code: 59, shift : 0, defer: true };				// JP ;+
				charCodeMap[59] = { code: 58, shift : 0, keyCode: 59 };
				shiftedKeyCodeMap[107] = { code: 59, shift : 0 };
				shiftedCharCodeMap[43] = { code: 42, shift : 0 };
		
				charCodeMap[43] = { code: 42, shift : 1, keyCode: 59 };
			} else {
				keyCodeMap[187] = { code: 59, shift : 0, defer: true };				// JP ;+
				charCodeMap[59] = { code: 58, shift : 0, keyCode: 59 };
				shiftedKeyCodeMap[187] = { code: 59, shift : 1 };
				shiftedCharCodeMap[43] = { code: 42, shift : 1 };
	
				keyCodeMap[107] = { code: 59, shift : 1 };							// JP NUM +
				charCodeMap[43] = { code: 42, shift : 1 };
			}
			
			if($.browser.mozilla) {
				keyCodeMap[59] = { code: 222, shift : 0 };							// JP :*
				charCodeMap[58] = { code: 39, shift : 0 };
				shiftedKeyCodeMap[59] = { code: 222, shift : 1 };
				shiftedCharCodeMap[42] = { code: 34, shift : 1 };
			} else {
				keyCodeMap[186] = { code: 222, shift : 0 };							// JP :*
				charCodeMap[58] = { code: 39, shift : 0 };
				shiftedKeyCodeMap[186] = { code: 222, shift : 1 };
				shiftedCharCodeMap[42] = { code: 34, shift : 1 };
			}
			
			keyCodeMap[221] = { code: 220, shift : 0 };								// JP ]}
			charCodeMap[93] = { code: 92, shift : 0 };
			shiftedKeyCodeMap[221] = { code: 220, shift : 1 };
			shiftedCharCodeMap[125] = { code: 124, shift : 1 };
			
			keyCodeMap[106] = { code: 222, shift : 1 };								// JP NUM *
			charCodeMap[42] = { code: 34, shift : 1 };
	
			keyCodeMap[110] = { code: 190, shift : 0 };								// JP NUM .
			charCodeMap[46] = { code: 46, shift : 0 };
				
			keyCodeMap[193] = { code: 220, shift : 0, charCode: 92 };				// JP key left to right shift on JP keyboard
			shiftedKeyCodeMap[193] = { code: 189, shift: 1, charCode: 64 };
			
			keyCodeMap[255] = { code: 220, shift : 0, charCode: 92 };				// JP Japanese Yen mark on JP keyboard
			shiftedKeyCodeMap[255] = { code: 220, shift: 1, charCode: 95 };
		}
		this.keyboardMappers[AjaxViewer.KEYBOARD_TYPE_JAPAN_JP_OS_TO_JP_VM] = new KeyboardMapper(false, keyCodeMap, shiftedKeyCodeMap, 
				charCodeMap, shiftedCharCodeMap);
	},
	
	getCurrentKeyboardMapper : function() {
		return this.keyboardMappers[this.currentKeyboard];
	},
	
	setupUIController : function() {
		var ajaxViewer = this;
		var pullDownElement = $("#toolbar").find(".pulldown");
		pullDownElement.hover(
			function(e) {
				var subMenu = pullDownElement.find("ul");
				var offset = subMenu.parent().offset();
				subMenu.css("left", offset.left);
			
				$("li.current").removeClass("current");
				$("li:eq(" + ajaxViewer.currentKeyboard + ")", subMenu).addClass("current");
				subMenu.css("z-index", "" + ajaxViewer.maxTileZIndex + 1).show();
				return false;
			},
			
			function(e) {
				pullDownElement.find("ul").hide();
				return false;
			}
		);

		$("[cmd]", "#toolbar").each(function(i, val) {
			$(val).click(function(e) {
				var cmd = $(e.target).attr("cmd");
				if(cmd)
					ajaxViewer.onCommand(cmd); 
				else {
					var cmdLink = $(e.target).closest("a");
					
					if(cmdLink.attr("cmd")) {
						var cmd = cmdLink.attr("cmd");
						ajaxViewer.onCommand(cmd);
					}
				}
			});
		});
	},
	
	onCommand : function(cmd) {
		if(cmd == "keyboard_jp_en_os_to_en_vm") {
			$("#toolbar").find(".pulldown").find("ul").hide();
			this.currentKeyboard = AjaxViewer.KEYBOARD_TYPE_JAPAN_EN_OS_TO_EN_VM;
		} else if(cmd == "keyboard_jp_jp_os_to_en_vm") {
			$("#toolbar").find(".pulldown").find("ul").hide();
			this.currentKeyboard = AjaxViewer.KEYBOARD_TYPE_JAPAN_JP_OS_TO_EN_VM;
		} else if(cmd == "keyboard_jp_en_os_to_jp_vm") {
			$("#toolbar").find(".pulldown").find("ul").hide();
			this.currentKeyboard = AjaxViewer.KEYBOARD_TYPE_JAPAN_EN_OS_TO_JP_VM;
		} else if(cmd == "keyboard_jp_jp_os_to_jp_vm") {
			$("#toolbar").find(".pulldown").find("ul").hide();
			this.currentKeyboard = AjaxViewer.KEYBOARD_TYPE_JAPAN_JP_OS_TO_JP_VM;
		} else if(cmd == "keyboard_en") {
			$("#toolbar").find(".pulldown").find("ul").hide();
			this.currentKeyboard = AjaxViewer.KEYBOARD_TYPE_ENGLISH;
		} else if(cmd == "sendCtrlAltDel") {
			this.sendKeyboardEvent(AjaxViewer.KEY_DOWN, 45, AjaxViewer.CTRL_KEY | AjaxViewer.ALT_KEY);
			this.sendKeyboardEvent(AjaxViewer.KEY_UP, 45, AjaxViewer.CTRL_KEY | AjaxViewer.ALT_KEY);
		} else if(cmd == "sendCtrlEsc") {
			this.sendKeyboardEvent(AjaxViewer.KEY_DOWN, 17, 0);
			this.sendKeyboardEvent(AjaxViewer.KEY_DOWN, 27, AjaxViewer.CTRL_KEY);
			this.sendKeyboardEvent(AjaxViewer.KEY_UP, 27, AjaxViewer.CTRL_KEY);
			this.sendKeyboardEvent(AjaxViewer.KEY_UP, 17, 0);
		} else if(cmd == "toggle_logwin") {
			if(!g_logger.isOpen()) {
				g_logger.enable(true);
				g_logger.open();
				g_logger.log(Logger.LEVEL_SYS, "Accept languages: " + acceptLanguages + ", current language: " + getCurrentLanguage());
			} else {
				g_logger.close();
			}
		}
	},
	
	// Firefox on Mac OS X does not generate key-code for following keys 
	translateZeroKeycode: function() {
		var len = this.eventQueue.length;
		if(len > 1 && this.eventQueue[len - 2].type == AjaxViewer.EVENT_QUEUE_KEYBOARD_EVENT && this.eventQueue[len - 2].code == 0) {
			switch(this.eventQueue[len - 1].code) {
			case 95 :	// underscore _
				this.eventQueue[len - 2].code = 109;
				break;
				
			case 58 :	// colon :
				this.eventQueue[len - 2].code = 59;
				break;
				
			case 60 : 	// <
				this.eventQueue[len - 2].code = 188;
				break;
				
			case 62 : 	// >
				this.eventQueue[len - 2].code = 190;
				break;
			
			case 63 :	// ?
				this.eventQueue[len - 2].code = 191;
				break;
				
			case 124 : 	// |
				this.eventQueue[len - 2].code = 220;
				break;
				
			case 126 :	// ~
				this.eventQueue[len - 2].code = 192;
				break;
				
			default :
				g_logger.log(Logger.LEVEL_WARN, "Zero keycode detected for KEY-PRESS char code " + this.eventQueue[len - 1].code);
				break;
			}
		}
	},

	//
	// Firefox on Mac OS X does not send KEY-DOWN for repeated KEY-PRESS event
	// IE on windows, when typing is fast, it will omit issuing KEY-DOWN event
	//
	translateImcompletedKeypress : function() {
		var len = this.eventQueue.length;
		if(len == 1 || !(this.eventQueue[len - 2].type == AjaxViewer.EVENT_QUEUE_KEYBOARD_EVENT && this.eventQueue[len - 2].event == AjaxViewer.KEY_DOWN)) {
			var nSplicePos = Math.max(0, len - 2);
			var keyPressEvent = this.eventQueue[len - 1];
			if(!!this.keyCodeMap[keyPressEvent.code]) {
				if(this.keyCodeMap[keyPressEvent.code].shift) {
					this.eventQueue.splice(nSplicePos, 0, {
						type: AjaxViewer.EVENT_QUEUE_KEYBOARD_EVENT,
						event: AjaxViewer.KEY_DOWN,
						code: this.keyCodeMap[keyPressEvent.code].code,
						modifiers: AjaxViewer.SHIFT_KEY
					});
				} else {
					this.eventQueue.splice(nSplicePos, 0, {
						type: AjaxViewer.EVENT_QUEUE_KEYBOARD_EVENT,
						event: AjaxViewer.KEY_DOWN,
						code: this.keyCodeMap[keyPressEvent.code].code,
						modifiers: 0
					});
				}
			} else {
				g_logger.log(Logger.LEVEL_WARN, "Keycode mapping is not defined to translate KEY-PRESS event for char code : " + keyPressEvent.code);
				this.eventQueue.splice(nSplicePos, 0, {
					type: AjaxViewer.EVENT_QUEUE_KEYBOARD_EVENT,
					event: AjaxViewer.KEY_DOWN,
					code: keyPressEvent.code,
					modifiers: keyPressEvent.modifiers
				});
			}
		}
	},
	
	sendKeyboardEvent: function(event, code, modifiers) {
		// back door to open logger window - CTRL-ATL-SHIFT+SPACE
		if(code == 32 && 
			(modifiers & AjaxViewer.SHIFT_KEY | AjaxViewer.CTRL_KEY | AjaxViewer.ALT_KEY) == (AjaxViewer.SHIFT_KEY | AjaxViewer.CTRL_KEY | AjaxViewer.ALT_KEY)) {
			
			if(!g_logger.isOpen()) {
				g_logger.enable(true);
				g_logger.open();
				g_logger.log(Logger.LEVEL_SYS, "Accept languages: " + acceptLanguages + ", current language: " + getCurrentLanguage());
			} else {
				g_logger.close();
			}
		}
			
		var len;
		g_logger.log(Logger.LEVEL_INFO, "Keyboard event: " + AjaxViewer.getEventName(event) + ", code: " + code + ", modifiers: " + modifiers + ', char: ' + String.fromCharCode(code));
		this.eventQueue.push({
			type: AjaxViewer.EVENT_QUEUE_KEYBOARD_EVENT,
			event: event,
			code: code,
			modifiers: modifiers
		});

		if(event == AjaxViewer.KEY_PRESS) {
			this.translateZeroKeycode();
			this.translateImcompletedKeypress();
		}
		
		if(this.rawKeyboard) {
			if(event == AjaxViewer.KEY_PRESS) {
				// special handling for key * in numeric pad area
				if(code == AjaxViewer.CHARCODE_NUMPAD_MULTIPLY) {
					len = this.eventQueue.length;
					if(len >= 2) {
						var origKeyDown = this.eventQueue[len - 2];
						if(origKeyDown.type == AjaxViewer.EVENT_QUEUE_KEYBOARD_EVENT && 
							origKeyDown.code == AjaxViewer.KEYCODE_MULTIPLY) {
							
							this.eventQueue.splice(len - 2, 2, {
								type: AjaxViewer.EVENT_QUEUE_KEYBOARD_EVENT,
								event: AjaxViewer.KEY_DOWN,
								code: AjaxViewer.KEYCODE_SHIFT,
								modifiers: 0
							},
							{
								type: AjaxViewer.EVENT_QUEUE_KEYBOARD_EVENT,
								event: AjaxViewer.KEY_DOWN,
								code: AjaxViewer.KEYCODE_8,
								modifiers: AjaxViewer.SHIFT_KEY
							},
							{
								type: AjaxViewer.EVENT_QUEUE_KEYBOARD_EVENT,
								event: AjaxViewer.KEY_UP,
								code: AjaxViewer.KEYCODE_8,
								modifiers: AjaxViewer.SHIFT_KEY
							},
							{
								type: AjaxViewer.EVENT_QUEUE_KEYBOARD_EVENT,
								event: AjaxViewer.KEY_UP,
								code: AjaxViewer.KEYCODE_SHIFT,
								modifiers: 0
							}
							);
						}
					}
					return;
				}
				
				// special handling for key + in numeric pad area
				if(code == AjaxViewer.CHARCODE_NUMPAD_ADD) {
					len = this.eventQueue.length;
					if(len >= 2) {
						var origKeyDown = this.eventQueue[len - 2];
						if(origKeyDown.type == AjaxViewer.EVENT_QUEUE_KEYBOARD_EVENT && 
							origKeyDown.code == AjaxViewer.KEYCODE_ADD) {

							g_logger.log(Logger.LEVEL_INFO, "Detected + on numeric pad area, fake it");
							this.eventQueue[len - 2].modifiers = AjaxViewer.SHIFT_KEY;	
							this.eventQueue[len - 1].modifiers = AjaxViewer.SHIFT_KEY;	
							this.eventQueue.splice(len - 2, 0, {
								type: AjaxViewer.EVENT_QUEUE_KEYBOARD_EVENT,
								event: AjaxViewer.KEY_DOWN,
								code: AjaxViewer.KEYCODE_SHIFT,
								modifiers: AjaxViewer.SHIFT_KEY
							});
							this.eventQueue.push({
								type: AjaxViewer.EVENT_QUEUE_KEYBOARD_EVENT,
								event: AjaxViewer.KEY_UP,
								code: AjaxViewer.KEYCODE_SHIFT,
								modifiers: AjaxViewer.SHIFT_KEY
							});
						}
					}
				}
			} 
			
			if(event != AjaxViewer.KEY_DOWN)
				this.checkEventQueue();
		} else {
			this.checkEventQueue();
		}
	},
	
	aggregateEvents: function() {
		var ajaxViewer = this;
		var aggratedQueue = [];
		
		var aggregating = false;
		var mouseX;
		var mouseY;
		$.each(ajaxViewer.eventQueue, function(index, item) {
			if(item.type != AjaxViewer.EVENT_QUEUE_MOUSE_EVENT) {
				aggratedQueue.push(item);
			} else {
				if(!aggregating) {
					if(item.event == AjaxViewer.MOUSE_MOVE) {
						aggregating = true;
						mouseX = item.x;
						mouseY = item.y;
					} else {
						aggratedQueue.push(item);
					}
				} else {
					if(item.event == AjaxViewer.MOUSE_MOVE) {
						// continue to aggregate mouse move event
						mouseX = item.x;
						mouseY = item.y;
					} else {
						aggratedQueue.push({
							type: AjaxViewer.EVENT_QUEUE_MOUSE_EVENT,
							event: AjaxViewer.MOUSE_MOVE,
							x: mouseX,
							y: mouseY,
							code: 0,
							modifiers: 0
						});
						aggregating = false;
						
						aggratedQueue.push(item);
					}
				}
			}
		});
		
		if(aggregating) {
			aggratedQueue.push({
				type: AjaxViewer.EVENT_QUEUE_MOUSE_EVENT,
				event: AjaxViewer.MOUSE_MOVE,
				x: mouseX,
				y: mouseY,
				code: 0,
				modifiers: 0
			});
		}
		
		this.eventQueue = aggratedQueue; 
	},
	
	checkEventQueue: function() {
		var ajaxViewer = this;
		
		if(!this.sendingEventInProgress && this.eventQueue.length > 0) {
			var sb = new StringBuilder();
			sb.append(""+this.eventQueue.length).append("|");
			$.each(this.eventQueue, function() {
				var item = this;
				if(item.type == AjaxViewer.EVENT_QUEUE_MOUSE_EVENT) {
					sb.append(""+item.type).append("|");
					sb.append(""+item.event).append("|");
					sb.append(""+item.x).append("|");
					sb.append(""+item.y).append("|");
					sb.append(""+item.code).append("|");
					sb.append(""+item.modifiers).append("|");
				} else {
					sb.append(""+item.type).append("|");
					sb.append(""+item.event).append("|");
					sb.append(""+item.code).append("|");
					sb.append(""+item.modifiers).append("|");
				}
			});
			this.eventQueue.length = 0;
			
			var url = ajaxViewer.updateUrl + "&event=" + AjaxViewer.EVENT_BAG;
			
			g_logger.log(Logger.LEVEL_TRACE, "Posting client event " + sb.toString() + "...");
			
			ajaxViewer.sendingEventInProgress = true;
			window.onStatusNotify(AjaxViewer.STATUS_SENDING);
			$.post(url, {data: sb.toString()}, function(data, textStatus) {
				g_logger.log(Logger.LEVEL_TRACE, "Client event " + sb.toString() + " is posted");
				
				ajaxViewer.sendingEventInProgress = false;
				window.onStatusNotify(AjaxViewer.STATUS_SENT);
				
				ajaxViewer.checkUpdate();
			}, 'html');
		}
	},
	
	onAjaxError: function(event, XMLHttpRequest, ajaxOptions, thrownError) {
		if(window.onClientError != undefined && jQuery.isFunction(window.onClientError)) {
			window.onClientError();
		}
	},
	
	onWindowResize: function() {
		var offset = this.panel.offset();
		
		var row = $('tr:first', this.panel);
		var cell = $('td:first', row);
		var tile = this.getTile(cell, 'tile');
		
		var tileOffset = tile.offset();
		var deltaX = offset.left - tileOffset.left;
		var deltaY = offset.top - tileOffset.top;
		
		if(deltaX != 0 || deltaY != 0) {
			$(".canvas_tile").each(function() {
				var offsetFrom = $(this).offset();
				$(this).css('left', offsetFrom.left + deltaX).css('top', offsetFrom.top + deltaY);
			});
		}
	},
	
	deleteCanvas: function() {
		$('.canvas_tile', $(document.body)).each(function() {
			$(this).remove();
		});
	},
	
	generateCanvas: function(wrapperDivId, width, height, tileWidth, tileHeight) {
		var canvasParent = $('#' + wrapperDivId);
		canvasParent.width(width);
		canvasParent.height(height);
		
		if(window.onCanvasSizeChange != undefined && jQuery.isFunction(window.onCanvasSizeChange))
			window.onCanvasSizeChange(width, height);
		
		var tableDef = '<table cellpadding="0px" cellspacing="0px">\r\n';
		var i = 0;
		var j = 0;
		for(i = 0; i < Math.ceil((height + tileHeight - 1) / tileHeight); i++) {
			var rowHeight = Math.min(height - i*tileHeight, tileHeight);
			tableDef += '<tr style="height:' + rowHeight + 'px">\r\n';
			
			for(j = 0; j < Math.ceil((width + tileWidth - 1) / tileWidth); j++) {
				var colWidth = Math.min(width - j*tileWidth, tileWidth);
				tableDef += '<td width="' + colWidth + 'px"></td>\r\n';
			}
			tableDef += '</tr>\r\n';
		}
		tableDef += '</table>\r\n';
		
		return $(tableDef).appendTo(canvasParent);
	},
	
	getTile: function(cell, name) {
		var clonedDiv = cell.data(name);
		if(!clonedDiv) {
			var offset = cell.offset();
			var divDef = "<div class=\"canvas_tile\" style=\"z-index:1;position:absolute;overflow:hidden;width:" + cell.width() + "px;height:" 
				+ cell.height() + "px;left:" + offset.left + "px;top:" + offset.top+"px\"></div>";
			
			clonedDiv = $(divDef).appendTo($(document.body));
			cell.data(name, clonedDiv);
		}
		
		return clonedDiv;
	},
	
	initCell: function(cell) {
		if(!cell.data("init")) {
			cell.data("init", true);
			
			cell.data("current", 0);
			this.getTile(cell, "tile2");
			this.getTile(cell, "tile");
		}
	},
	
	displayCell: function(cell, bg) {
		var div;
		var divPrev;
		if(!cell.data("current")) {
			cell.data("current", 1);
			
			divPrev = this.getTile(cell, "tile");
			div = this.getTile(cell, "tile2");
		} else {
			cell.data("current", 0);
			divPrev = this.getTile(cell, "tile2");
			div = this.getTile(cell, "tile");
		}
		
		var zIndex = parseInt(divPrev.css("z-index")) + 1;
		this.maxTileZIndex = Math.max(this.maxTileZIndex, zIndex);
		div.css("z-index", zIndex);
		div.css("background", bg);
	},
	
	updateTile: function() {
		if(this.dirty) {
			var ajaxViewer = this;
			var tileWidth = this.tileWidth;
			var tileHeight = this.tileHeight;
			var imgUrl = this.imgUrl;
			var panel = this.panel;
			
			if(this.fullImage) {
				$.each(this.tileMap, function() {
					var i = $(this)[0];
					var j = $(this)[1];
					var row = $("TR:eq("+i+")", panel);
					var cell = $("TD:eq("+j+")", row);
					var attr = "url(" + imgUrl + ") -"+j*tileWidth+"px -"+i*tileHeight + "px";
					
					ajaxViewer.initCell(cell);
					ajaxViewer.displayCell(cell, attr);
				});
			} else {
				$.each(this.tileMap, function(index) {
					var i = $(this)[0];
					var j = $(this)[1];
					var offset = index*tileWidth;
					var attr = "url(" + imgUrl + ") no-repeat -"+offset+"px 0px";
					var row = $("TR:eq("+i+")", panel);
					var cell = $("TD:eq("+j+")", row);
					
					ajaxViewer.initCell(cell);
					ajaxViewer.displayCell(cell, attr);
				});
			}
			
			this.dirty = false;
		}
	},
	
	heartbeat: function() {
		this.checkEventQueue();
		this.checkUpdate();
	},
	
	checkUpdate: function() {
		if(!this.isDirty())
			return;
		
		if(this.isImageLoaded()) {
			this.updateTile();
			var url = this.updateUrl;
			var ajaxViewer = this;

			window.onStatusNotify(AjaxViewer.STATUS_RECEIVING);
			$.getScript(url, function(data, textStatus) {
				if(/^<html>/.test(data)) {
					ajaxViewer.stop();
					$(document.body).html(data);
				} else {
					eval(data);
					ajaxViewer.setDirty(true);
					window.onStatusNotify(AjaxViewer.STATUS_RECEIVED);
					
					ajaxViewer.checkUpdate();
				}
			});
		} 
	},
	
	ptInPanel: function(pageX, pageY) {
		var mainPanel = this.panel;
		
		var offset = mainPanel.offset();
		var x = pageX - offset.left;
		var y = pageY - offset.top;
		
		if(x < 0 || y < 0 || x > mainPanel.width() - 1 || y > mainPanel.height() - 1)
			return false;
		return true;
	},
	
	pageToPanel: function(pageX, pageY) {
		var mainPanel = this.panel;
		
		var offset = mainPanel.offset();
		var x = pageX - offset.left;
		var y = pageY - offset.top;
		
		if(x < 0)
			x = 0;
		if(x > mainPanel.width() - 1)
			x = mainPanel.width() - 1;
		
		if(y < 0)
			y = 0;
		if(y > mainPanel.height() - 1)
			y = mainPanel.height() - 1;
		
		return { x: Math.ceil(x), y: Math.ceil(y) };
	},
	
	installMouseHook: function() {
		var ajaxViewer = this;
		var target = $(document.body);
		
		target.mousemove(function(e) {
			if(!ajaxViewer.ptInPanel(e.pageX, e.pageY))
				return true;
			
			var pt = ajaxViewer.pageToPanel(e.pageX, e.pageY);  
			ajaxViewer.onMouseMove(pt.x, pt.y);
			
			e.stopPropagation();
			return false;
		});
		
		target.mousedown(function(e) {
			ajaxViewer.panel.parent().focus();
			
			if(!ajaxViewer.ptInPanel(e.pageX, e.pageY))
				return true;
			
			var modifiers = ajaxViewer.getKeyModifiers(e);
			var whichButton = e.button;
			
			var pt = ajaxViewer.pageToPanel(e.pageX, e.pageY);  
			ajaxViewer.onMouseDown(pt.x, pt.y, whichButton, modifiers);
			
			e.stopPropagation();
			return false;
		});
		
		target.mouseup(function(e) {
			if(!ajaxViewer.ptInPanel(e.pageX, e.pageY))
				return true;
			
			var modifiers = ajaxViewer.getKeyModifiers(e);
			var whichButton = e.button;
			
			var pt = ajaxViewer.pageToPanel(e.pageX, e.pageY);  

			ajaxViewer.onMouseUp(pt.x, pt.y, whichButton, modifiers);
			e.stopPropagation();
			return false;
		});
		
		// disable browser right-click context menu
		target.bind("contextmenu", function() { return false; });
	},
	
	uninstallMouseHook : function() {
		var target = $(document);
		target.unbind("mousemove");
		target.unbind("mousedown");
		target.unbind("mouseup");
		target.unbind("contextmenu");
	},
	
	requiresDefaultKeyProcess : function(e) {
		switch(e.which) {
		case 8 :		// backspace
		case 9 :		// TAB
		case 19 :		// PAUSE/BREAK
		case 20 :		// CAPSLOCK
		case 27 :		// ESCAPE
		case 16 :		// SHIFT key
		case 17 :		// CTRL key
		case 18 :		// ALT key
		case 33 :		// PGUP
		case 34 :		// PGDN
		case 35 :		// END
		case 36 :		// HOME
		case 37 :		// LEFT
		case 38 :		// UP
		case 39 :		// RIGHT
		case 40 :		// DOWN
			return false;
		}
		
		if(this.getKeyModifiers(e) == AjaxViewer.SHIFT_KEY)
			return true;
		
		if(this.getKeyModifiers(e) != 0)
			return false;
		
		return true;
	},
	
	installKeyboardHook: function() {
		var ajaxViewer = this;
		var target = $(document);

		target.keypress(function(e) {
			ajaxViewer.onKeyPress(e.which, ajaxViewer.getKeyModifiers(e));

			e.stopPropagation();
			if(ajaxViewer.requiresDefaultKeyProcess(e))
				return true;
			
			e.preventDefault();
			return false;
		});
		
		target.keydown(function(e) {
			ajaxViewer.onKeyDown(e.which, ajaxViewer.getKeyModifiers(e));
			
			e.stopPropagation();
			if(ajaxViewer.requiresDefaultKeyProcess(e))
				return true;
			
			e.preventDefault();
			return false;
		});
		
		target.keyup(function(e) {
			ajaxViewer.onKeyUp(e.which, ajaxViewer.getKeyModifiers(e));

			e.stopPropagation();
			if(ajaxViewer.requiresDefaultKeyProcess(e))
				return true;
			
			e.preventDefault();
			return false;
		});
	},
	
	uninstallKeyboardHook : function() {
		var target = $(document);
		target.unbind("keypress");
		target.unbind("keydown");
		target.unbind("keyup");
	},
	
	onMouseMove: function(x, y) {
		this.sendMouseEvent(AjaxViewer.MOUSE_MOVE, x, y, 0, 0);
	},
	
	onMouseDown: function(x, y, whichButton, modifiers) {
		this.sendMouseEvent(AjaxViewer.MOUSE_DOWN, x, y, whichButton, modifiers);
	},
	
	onMouseUp: function(x, y, whichButton, modifiers) {
		this.sendMouseEvent(AjaxViewer.MOUSE_UP, x, y, whichButton, modifiers);
		
		var curTick = new Date().getTime();
		if(this.lastClickEvent.time && (curTick - this.lastClickEvent.time < 300)) {
			this.onMouseDblClick(this.lastClickEvent.x, this.lastClickEvent.y, 
				this.lastClickEvent.button, this.lastClickEvent.modifiers);
		}
		
		this.lastClickEvent.x = x;
		this.lastClickEvent.y = y;
		this.lastClickEvent.button = whichButton;
		this.lastClickEvent.modifiers = modifiers;
		this.lastClickEvent.time = curTick;
	},
	
	onMouseDblClick: function(x, y, whichButton, modifiers) {
		this.sendMouseEvent(AjaxViewer.MOUSE_DBLCLK, x, y, whichButton, modifiers);
	},
	
	onKeyPress: function(code, modifiers) {
		this.dispatchKeyboardInput(AjaxViewer.KEY_PRESS, code, modifiers);
	},
	
	onKeyDown: function(code, modifiers) {
		this.dispatchKeyboardInput(AjaxViewer.KEY_DOWN, code, modifiers);
	},
	
	onKeyUp: function(code, modifiers) {
		this.dispatchKeyboardInput(AjaxViewer.KEY_UP, code, modifiers);
	},
	
	dispatchKeyboardInput : function(event, code, modifiers) {
		var keyboardMapper = ajaxViewer.getCurrentKeyboardMapper();
		keyboardMapper.inputFeed(event, code, modifiers);
		this.dispatchMappedKeyboardInput(keyboardMapper.getMappedInput());
	},
	
	dispatchMappedKeyboardInput : function(mappedInput) {
		for(var i = 0; i < mappedInput.length; i++) {
			switch(mappedInput[i].type) {
			case AjaxViewer.KEY_DOWN :
				this.sendKeyboardEvent(AjaxViewer.KEY_DOWN, mappedInput[i].code, mappedInput[i].modifiers);
				break;
				
			case AjaxViewer.KEY_UP :
				this.sendKeyboardEvent(AjaxViewer.KEY_UP, mappedInput[i].code, mappedInput[i].modifiers);
				break;
				
			case AjaxViewer.KEY_PRESS :
				this.sendKeyboardEvent(AjaxViewer.KEY_PRESS, mappedInput[i].code, mappedInput[i].modifiers);
				break;
			}
		}
	},
	
	getKeyModifiers: function(e) {
		var modifiers = 0;
		if(e.altKey)
			modifiers |= AjaxViewer.ALT_KEY;
		
		if(e.altLeft)
			modifiers |= AjaxViewer.ALT_LEFT;
		
		if(e.ctrlKey)
			modifiers |= AjaxViewer.CTRL_KEY;
		
		if(e.ctrlLeft)
			modifiers |=  AjaxViewer.CTRL_LEFT;
		
		if(e.shiftKey)
			modifiers |=  AjaxViewer.SHIFT_KEY;
		
		if(e.shiftLeft)
			modifiers |= AjaxViewer.SHIFT_LEFT;
		
		if(e.metaKey)
			modifiers |= AjaxViewer.META_KEY;
		
		return modifiers;
	}
};
