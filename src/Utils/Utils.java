package Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Iterator;

public class Utils {

	private static Charset charset = Charset.forName("UTF-8");

	public static CharSequence bytesToHex(byte[] in) {

		return bytesToHex(in, "0x", " ");
	}

	public static CharSequence bytesToHex(byte[] in, CharSequence pre) {

		return bytesToHex(in, pre, " ");
	}

	public static CharSequence bytesToHex(byte[] in, CharSequence pre, CharSequence post) {

		StringBuilder sb = new StringBuilder();
		boolean isFirst = true;

		for (byte b : in) {
			if (isFirst) {
				isFirst = false;
			} else {
				sb.append(post);
			}
			sb.append(pre);
			sb.append(String.format("%02x", b));
		}
		return sb;
	}

	public static byte[] toByteArray(boolean b) {

		return toByteArray(Boolean.toString(b));
	}

	public static byte[] toByteArray(CharSequence string) {

		if (isEmpty(string)) {
			return new byte[] {};
		}

		return toByteArray(string.toString().toCharArray());
	}

	public static byte[] toByteArray(char[] charArray) {

		return toByteArray(charArray, charset);
	}

	protected static byte[] toByteArray(char[] charArray, Charset charset) {

		if (isEmpty(charArray)) {
			return new byte[] {};
		}

		CharBuffer cbuf = CharBuffer.wrap(charArray);
		ByteBuffer bbuf = charset.encode(cbuf);
		return Arrays.copyOf(bbuf.array(), charArray.length);
	}

	public static char[] toCharArray(byte[] bt) {

		int length = bt.length;
		char[] ch = new char[length];
		for (int i = 0; i < length; i++) {
			ch[i] = (char) bt[i];
		}
		return ch;
	}

	public static byte[][] splitBytes(byte[] data, int chunkSize) {

		int length = data.length;
		byte[][] dest = new byte[(length + chunkSize - 1) / chunkSize][];
		int destIndex = 0;
		int stopIndex = 0;

		for (int startIndex = 0; startIndex + chunkSize <= length; startIndex += chunkSize) {
			stopIndex += chunkSize;
			dest[destIndex++] = Arrays.copyOfRange(data, startIndex, stopIndex);
		}

		if (stopIndex < length) {
			dest[destIndex] = Arrays.copyOfRange(data, stopIndex, length);
		}

		return dest;
	}

	public static CharSequence getFileExtension(File file) {

		String fileName = file.getName();
		if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0) {
			return fileName.substring(fileName.lastIndexOf(".") + 1);
		} else {
			return "";
		}
	}

	public static void copyFile(File source, File dest) throws IOException, FileNotFoundException {

		InputStream is = null;
		OutputStream os = null;

		if (!isFileExists(source)) {
			return;
		}

		try {
			is = new FileInputStream(source);
			os = new FileOutputStream(dest, false);
			byte[] buffer = new byte[1024];
			int length;
			while ((length = is.read(buffer)) > 0) {
				os.write(buffer, 0, length);
			}
		} finally {
			if (is == null || os == null) {
				throw new IOException("");
			}
			is.close();
			os.close();
		}
	}
	
    public static URI getBuildDirURI(String fileName) throws URISyntaxException, IOException {

        if (fileName == null) {
            fileName = "";
        }

        File file = new File(System.getProperty("user.dir") + File.separator + fileName);
        if (file.exists()) {
            return file.toURI();
        } else {
            file = new File(System.getProperty("user.dir") + File.separator + "build" + File.separator + fileName);
            if (file.exists()) {
                return file.toURI();
            }
            return null;
        }
    }

	public static boolean isEmpty(CharSequence s) {

		return s == null || s.length() == 0;
	}

	public static boolean isEmpty(char[] s) {

		return s == null || s.length == 0;
	}

	public static boolean isEmpty(byte[]... arrays) {

		return arrays == null || arrays.length == 0;
	}

	public static boolean isEmpty(byte[] s) {

		return s == null || s.length == 0;
	}

	public static boolean isEmpty(Iterable<?> i) {

		return i == null || isEmpty(i.iterator());
	}

	public static boolean isEmpty(Iterator<?> it) {

		return it == null || !it.hasNext();
	}

	public static boolean isEmpty(Object[] array) {

		return array == null || array.length == 0;
	}

	public static boolean isFileExists(File file) {

		return file != null && file.exists();
	}

	public static boolean isFileExists(URI file) {

		return isFileExists(new File(file));
	}

	public static CharSequence toString(byte[] s) {

		return isEmpty(s) ? "" : toString(s, 0, s.length, charset);
	}

	public static CharSequence toString(byte[] s, int offset, int length) {

		return isEmpty(s) ? "" : toString(s, offset, length, charset);
	}

	protected static CharSequence toString(byte[] s, int offset, int length, Charset charset) {

		return isEmpty(s) ? "" : new String(Arrays.copyOfRange(s, offset, length), charset);
	}

	public static CharSequence getDuration(long duration) {

		long h, m, s, ms;
		long temp = duration;

		h = temp / 3600000;
		temp = temp - h * 3600000;
		m = temp / 60000;
		temp = temp - m * 60000;
		s = temp / 1000;
		temp = temp - s * 1000;
		ms = temp;

		StringBuilder sb = new StringBuilder();
		if (h > 0) {
			sb.append(h);
			sb.append("h ");
		}
		if (m > 0) {
			sb.append(m);
			sb.append("m ");
		}
		if (s > 0) {
			sb.append(s);
			sb.append("s ");
		}
		if (ms > 0) {
			sb.append(ms);
			sb.append("ms");
		}
		if (sb.length() == 0) {
			sb.append("0s");
		}
		return sb.toString();
	}

	public static byte[] convertToByteArray(String s) {

		int length = s.length();

		byte[] array = new byte[length];

		for (int i = 0; i < length; i++) {
			int j = Character.digit(s.charAt(i), 10);
			array[i] = (byte) j;
		}
		return array;
	}

	public static void Sleep(long millis) {

		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
}
