import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class Main {

    public static void main(String[] args) throws Exception{
//get the localhost IP address, if server is running on some other IP, you need to use that
        //InetAddress host = InetAddress.getLocalHost();
        Socket socket = null;
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;
        for(int i=0; i<100;i++){
            //establish socket connection to server
            //socket = new Socket(host.getHostName(), 9876);
            socket = new Socket("192.168.0.101", 8989);
            //write to socket using ObjectOutputStream
            oos = new ObjectOutputStream(socket.getOutputStream());
            System.out.println("Sending request to Socket Server");
            if(i%5==0)
                oos.writeObject("exit");
            else{
                //oos.writeObject(""+i);
                oos.writeObject(Integer.valueOf(i).toString());
            }
            //read the server response message
            //ois = new ObjectInputStream(socket.getInputStream());
            //String message = (String) ois.readObject();
            //System.out.println("Message: " + message);
            //close resources
            //ois.close();
            oos.close();
            socket.close();
            Thread.sleep(3*1000);
            //Thread.sleep(20);
        }
    }

}
