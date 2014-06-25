
/*
 * @author Diogo Anjos (diogo.silva.anjos@tecnico.ulisboa.pt)
 */

public class DataAcquisitionDriver implements Runnable {

    @Override
    public void run() {
        while (true) {
            System.out.println("Thread is runnig!");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

}
