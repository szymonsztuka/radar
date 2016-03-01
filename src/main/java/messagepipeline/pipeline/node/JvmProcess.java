package messagepipeline.pipeline.node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class JvmProcess implements Runnable, Node {

    private static final Logger logger = LoggerFactory.getLogger(JvmProcess.class);
    final private CyclicBarrier batchStart;
    final private CyclicBarrier batchEnd;
    final private String[] jvmArguments;
    final private String[] programArguments;
    final private String classpath;
    final private String mainClass;
    private volatile boolean process = true;
    private final String name;
    private String processLogFile;
    public JvmProcess(String name, String classpath, String[] jvmArguments, String mainClass, String[] programArguments, String processLogFile, CyclicBarrier batchStart, CyclicBarrier batchEnd) {
        this.name = name;
        this.batchStart = batchStart;
        this.batchEnd = batchEnd;
        this.classpath = classpath;
        this.jvmArguments = jvmArguments;
        this.programArguments = programArguments;
        this.mainClass = mainClass;
        this.processLogFile = processLogFile;
    }

    public int exec(String classCanonicalName, String classpath, String[] jvmArguments, String[] programArguments, CyclicBarrier batchStart, CyclicBarrier batchEnd) throws IOException,
            InterruptedException {
        int exitValue = 0;
        int i=0;
        while(i < 2) {
            i++;

        String javaHome = System.getProperty("java.home");
        String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
        List<String> allArguments = new ArrayList<>(3);
        allArguments.add(javaBin);
        allArguments.add("-cp");
        allArguments.add(classpath);
        if (null != jvmArguments && jvmArguments.length > 0 && !"".equals(jvmArguments[0])) {
            allArguments.addAll(Arrays.asList(jvmArguments));
        }
        allArguments.add(classCanonicalName);
        if (null != programArguments && programArguments.length > 0) {
            allArguments.addAll(Arrays.asList(programArguments));
        }
        logger.info(allArguments.toString());
        ProcessBuilder builder = new ProcessBuilder(allArguments);
        if (logger.isTraceEnabled()) {
            for (String elem : builder.command()) {
                logger.trace(elem);
            }
        }
        builder.redirectErrorStream(true);
        File output = new File(processLogFile+i);//"logs/process.log");
        builder.redirectOutput(output);
        java.lang.Process process = builder.start();
        new Thread(() -> {
             {
                Scanner sc = new Scanner(process.getInputStream());
                while (sc.hasNextLine()) {
                    //dest.println(sc.nextLine());
                    sc.nextLine();
                }
            }
        }).start();
        Thread.sleep(1000*20);
        //logger.debug("batchStart.await()");
        try {
            batchStart.await();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        }
        //logger.debug("batchStart.await() -done ");
        //logger.debug("batchEnd.await()!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        try {
            batchEnd.await();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        }

        Thread.sleep(1000);
        logger.debug("shouting down step "+i);

        process.destroy();

        process.waitFor();
        Thread.sleep(1000);
            exitValue = process.exitValue();
        }
        return exitValue;
    }

    public void run() {

            //long startTime = 0;
            try {
               //
                //startTime = System.nanoTime();
                int status = exec(mainClass, classpath, jvmArguments, programArguments, batchStart, batchEnd);
                logger.info("returned status " + status);
                //long endTime = System.nanoTime();
                //long duration = endTime - startTime;
                //double seconds = (duration / 1000000000.0);
            } catch (IOException ex) {
                //long endTime = System.nanoTime();
                //long duration = endTime - startTime;
                //double seconds = (duration / 1000000000.0);
                logger.error(ex.getMessage(), ex);
            } catch (InterruptedException ex) {
                //long endTime = System.nanoTime();
                //long duration = endTime - startTime;
                //double seconds = (duration / 1000000000.0);
                logger.error(ex.getMessage(), ex);
            }

    }

    public void signalBatchEnd() {
        process = false;
        //logger.trace("process set to " + process);
    }

    public String getName(){
        return this.name;
    }
}