package serverchainsimulator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class LayerControllerRecursive implements Runnable, LayerControllerDecorator {

    private final CyclicBarrier batchStart;
    private final CyclicBarrier batchEnd;
    private final List<? extends Stopable> nodes;


    final private LayerControllerDecorator next;
    private static final Logger logger = LoggerFactory.getLogger(LayerControllerRecursive.class);
    private final String name;

    public LayerControllerRecursive(String name, CyclicBarrier batchStart, CyclicBarrier batchEnd, List<? extends Stopable> nodes, LayerControllerDecorator next) {
        this.batchStart = batchStart;
        this.batchEnd = batchEnd;
        this.nodes = nodes;
        this.next = next;
        this.name = name;
    }

    public boolean step(){
        logger.info(name + " batch start awaits !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        boolean result =false;
        try {
            batchStart.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        }
        logger.info(name + " batch start got througth awaits !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        if(next!=null) {
            result = next.step();
        }
        nodes.forEach(Stopable::signalBatchEnd);
        logger.info(name + " batch end awaits !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        try {
            batchEnd.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        }
        logger.info(name + " batch end got througth awaits !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        return result;
    }

    @Override
    public void run() {
        boolean run = true;
        //while (!allProducersDone()) {


        //}
        while(run) {
            run = step();
        }
        logger.info("done");
    }

}