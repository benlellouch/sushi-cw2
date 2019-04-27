package comp1206.sushi;


import comp1206.sushi.server.Server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class DataPersistence implements Runnable {

    Server server;
    ObjectOutputStream oos;

    public DataPersistence(Server server) throws IOException {
        this.server = server;
        File outFile = new File( "SerialOutput.data" );
        FileOutputStream fos = new FileOutputStream(outFile);
        this.oos = new ObjectOutputStream(fos);

    }

    @Override
    public void run(){
        while(true){
            try{
                Thread.sleep(1000);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
            try {
                oos.writeObject(server.postcodes);
                oos.writeObject(server.restaurant);
                oos.writeObject(server.suppliers);
                oos.writeObject(server.ingredients);
                oos.writeObject(server.dishes);
                oos.writeObject(server.users);
                oos.writeObject(server.getIngredientStockLevels());
                oos.writeObject(server.getDishStockLevels());
                oos.writeObject(server.staff);
                oos.writeObject(server.drones);


            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }
}
