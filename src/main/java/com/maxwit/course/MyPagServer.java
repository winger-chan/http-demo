import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class MyPagServer {
    public static StringBuilder inputStream(Socket socket, InputStream inputStream) throws IOException {
        byte[] bytes = new byte[1024];
        int len = inputStream.read(bytes);
        StringBuilder ss = new StringBuilder();
        ss.append(new String(bytes, 0, len, "UTF-8"));

        return ss;
    }

    public static File findFile(String path) {
        // getting the working directory of the current program in Java
        String currentPath = System.getProperty("user.dir");
        // use class File to fing a file
        return new File(currentPath + path);
    }

    public static Map<String, String> getMap(File f) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("Content-Length: ", String.valueOf(f.length()));

        return map;
    }

    public static void outputStream(Socket socket, OutputStream outputStream, String repHttp, int repNum,
            String repStatu, File f) throws IOException {
        if (f.isDirectory()) {
            repNum = 404;
            repStatu = "Not Found";
            f = findFile("/aa/findDirectory.html");
        } else if (!f.exists()) {
            repNum = 404;
            repStatu = "Not Found";
            f = findFile("/aa/Error.html");
        }

        String status = repHttp + " " + repNum + " " + repStatu + "\n";
        outputStream.write(status.getBytes("UTF-8"));

        Map<String, String> map = getMap(f);
        String header = "";
        for (String key : map.keySet()) {
            header += key + map.get(key) + "\n";
        }
        header += "\n";
        outputStream.write(header.getBytes("UTF-8"));

        InputStream fileInput = new FileInputStream(f);
        byte[] bytes = new byte[1024];
        int len;

        while ((len = fileInput.read(bytes)) != -1) {
            outputStream.write(bytes, 0, len);
            outputStream.flush();
        }

        fileInput.close();
    }

    public static File fileContent(StringBuilder ss) {

        String line = ss.toString();
        String[] list = line.split(" ");
        String file = list[1];
        return findFile(file);
    }

    public static void main(String[] args) throws IOException {
        String repHttp = "HTTP/1.1";
        int repNum = 200;
        String repStatu = "OK";

        while (true) {
            ServerSocket server = new ServerSocket(Integer.parseInt(args[0]));
            Socket socket = server.accept();

            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();

            StringBuilder ss = inputStream(socket, inputStream);
            
            File file = fileContent(ss);
            outputStream(socket, outputStream, repHttp, repNum, repStatu, file);

            inputStream.close();
            outputStream.close();
            socket.close();
            server.close();
        }
    }
}
