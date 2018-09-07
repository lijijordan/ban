package ws;/*
 * Copyright (c) 2010-2018 Nathan Rajlich
 *
 *  Permission is hereby granted, free of charge, to any person
 *  obtaining a copy of this software and associated documentation
 *  files (the "Software"), to deal in the Software without
 *  restriction, including without limitation the rights to use,
 *  copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the
 *  Software is furnished to do so, subject to the following
 *  conditions:
 *
 *  The above copyright notice and this permission notice shall be
 *  included in all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *  OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 *  HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *  WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 *  FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 *  OTHER DEALINGS IN THE SOFTWARE.
 */

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

/** This example demonstrates how to create a websocket connection to a server. Only the most important callbacks are overloaded. */
public class FcoinClient extends WebSocketClient {

	public FcoinClient(URI serverUri , Draft draft ) {
		super( serverUri, draft );
	}

	public FcoinClient(URI serverURI ) {
		super( serverURI );
	}

	public FcoinClient(URI serverUri, Map<String, String> httpHeaders ) {
		super(serverUri, httpHeaders);
	}

	@Override
	public void onOpen( ServerHandshake handshakedata ) {
//		send("Hello, it is me. Mario :)");
//		send("{\"cmd\": \"login\", \"value\": \"{\\\"path\\\": \\\"/ws\\\", \\\"headers\\\": {\\\"token\\\": \\\"A8dPdAIWbOZXZC/QVK7PJ/pRmeg=\\\", \\\"Date\\\": \\\"Thu, 17 May 2018 06:17:45 GMT\\\", \\\"Content-Type\\\": \\\"application/json\\\", \\\"Auth\\\": \\\"42be04a2f49e507db56b7ca65a64acac:3A2S8Q/zVCkLGEBdbk8HDY3BolI=\\\"}, \\\"method\\\": \\\"\\\"}\"}");
		// {"topic":"depth.L20.ethbtc"}

		//{"type": "topics", "topics": ["depth.L20.ethbtc", "depth.L100.btcusdt"]}
		System.out.println( "opened connection" );
		//"{"cmd":"topic"}"
//		send("\"{\"topic\":\"123\"}");
		send("{\"cmd\":\"sub\",\"args\":[\"depth.L20.ethusdt\"],\"id\":\"1\"}");

		// if you plan to refuse connection based on ip or httpfields overload: onWebsocketHandshakeReceivedAsClient
	}

	@Override
	public void onMessage( String message ) {
		System.out.println( "received: " + message );
	}

	@Override
	public void onClose(int code, String reason, boolean remote ) {
		// The codecodes are documented in class org.java_websocket.framing.CloseFrame
		System.out.println( "Connection closed by " + ( remote ? "remote peer" : "us" ) + " Code: " + code + " Reason: " + reason );
	}

	@Override
	public void onError( Exception ex ) {
		ex.printStackTrace();
		// if the error is fatal then onClose will be called additionally
	}

	public static void main( String[] args ) throws URISyntaxException {
		FcoinClient c = new FcoinClient( new URI( "wss://api.fcoin.com/v2/ws" )); // more about drafts here: http://github.com/TooTallNate/Java-WebSocket/wiki/Drafts
		c.setConnectionLostTimeout(30);
		c.connect();

	}

}