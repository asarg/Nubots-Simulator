import org.javatuples.Pair;
import org.monte.media.quicktime.QuickTimeWriter;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Driver {
    Thread simHeartBeat;
    Runnable simRunnable;
    RecordRunnable recordRunnable;
    MainFrame mainFrame;
    NubotCanvas simNubotCanvas;
    Configuration map = Configuration.getInstance();
   Configuration mapCC;
   ExecutorService execServ;

    //Video
    public double nubRatio =0;

    public NubotVideo nubotVideo;


    public Driver(final Dimension size)
    {


        mainFrame = new MainFrame(size,this );
        simNubotCanvas = NubotCanvas.getSimInstance();

        simRunnable = new Runnable() {
            @Override
            public void run() {

                  
                simNubotCanvas.repaint();
              

                while (map.simulation.isRunning) {
                    try {

                        if (!map.simulation.isRecording)
                            Thread.sleep((long) (30+  map.simulation.speedRate * 1000.0 * map.timeStep));





                        if (map.simulation.animate) {
                            map.executeFrame();
                            mainFrame.renderNubot(map.values());

                        }





                        mainFrame.setStatus("Simulating...", null,null, "Monomers: "  + map.getSize(), "Time: " + map.timeElapsed, "Step: " + map.markovStep);
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }
                try
                {

                }
                catch(Exception e)
                {
                    System.out.println("exception closing");
                }


                mainFrame.setStatus("Simulation finished ", null, null, null, null, null);
                if (map.isFinished )
                {






                    JOptionPane.showMessageDialog(simNubotCanvas, "No more rules can be applied!", "Finished", JOptionPane.OK_OPTION);
                }
            }
        };
    }

    public void createMapCC()
    {
       mapCC = map.getCopy();
    }
    public void simStart()
    {
        simStop();
        simHeartBeat = new Thread(simRunnable);
        simHeartBeat.start();
    }
    public void simStop()
    {
        if(simHeartBeat !=null && simHeartBeat.isAlive())
            simHeartBeat.interrupt();
    }
   public void recordSim(String vidName, int numRecords, int recordLength, boolean toEnd, double ratio, boolean RtoN)
   {

    execServ = Executors.newFixedThreadPool(numRecords);

      for(int i = 1; i <= numRecords; i++)
      {
         Configuration rMap=   mapCC.getCopy();
         rMap.simulation.recordingLength= recordLength;
         rMap.simulation.isRecording = true;
         rMap.simulation.isRunning = true;


        execServ.submit(new RecordRunnable(new NubotVideo(800,600, QuickTimeWriter.VIDEO_PNG,20, vidName + i), rMap, ratio));

      }
      execServ.shutdown();



   }






}
