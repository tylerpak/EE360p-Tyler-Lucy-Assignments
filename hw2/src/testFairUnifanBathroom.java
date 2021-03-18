//Test driver code for question 2. Should not be submitted


import java.util.concurrent.TimeUnit;

public class testFairUnifanBathroom {
    final static FairUnifanBathroom bathroom = new FairUnifanBathroom();

    public static void main(String[] args) {
        UTfan ut1 = new UTfan();
        UTfan ut2 = new UTfan();
        UTfan ut3 = new UTfan();
        UTfan ut4 = new UTfan();
        UTfan ut5 = new UTfan();
        OUfan ou1 = new OUfan();
        OUfan ou2 = new OUfan();
        OUfan ou3 = new OUfan();
        OUfan ou4 = new OUfan();
        OUfan ou5 = new OUfan();
        ut1.start();
        ut2.start();
        ut3.start();
        ou1.start();
        ut4.start();
        ut5.start();
        ou2.start();
        ou3.start();
        ou4.start();
        ou5.start();
    }




    static class UTfan extends Thread {

        @Override
        public void run() {
            bathroom.enterBathroomUT();
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            bathroom.leaveBathroomUT();
        }
    }

    static class OUfan extends Thread {

        @Override
        public void run() {
            bathroom.enterBathroomOU();
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            bathroom.leaveBathroomOU();
        }
    }

}

