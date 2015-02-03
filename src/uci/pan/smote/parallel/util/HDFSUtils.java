package uci.pan.smote.parallel.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

public class HDFSUtils {


	private static FileSystem getFileSystem(String uri) throws Exception {
		Configuration conf = new Configuration();
		return FileSystem.get(URI.create(uri), conf);
		//return new Path(uri).getFileSystem(conf);
	}
	
	
	public static InputStream getInputStream(String uri) {
		try {
			return getFileSystem(uri).open(new Path(uri));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static OutputStream getOutputStream(String uri) {
		try {
			return getFileSystem(uri).create(new Path(uri), true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static boolean exists(String uri) {
		try {
			return getFileSystem(uri).exists(new Path(uri));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public static void writeToFile(String fileUri, String content) throws Exception {
		PrintStream out = new PrintStream(getOutputStream(fileUri));
		out.print(content);
		out.close();
	}

	public static boolean mkdir(String uri) throws Exception {
		return getFileSystem(uri).mkdirs(new Path(uri));
	}
}
