package com.cs.saurabh.chatsnippet.client;

import android.os.Looper;
import android.util.Log;

import com.cs.saurabh.chatsnippet.client.service.IService;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by saurabhATchampasheruDOTbuild on 15/5/16.
 * The abstract implementation of the Client facade. Note that this is not tied to any particular app/domain like, Haptik.
 * Haptik or any other app shall provide more specific implementation(more on the semantics side) of this class by extending it.
 * This class provides more of boiler-plate code which sets up looper, thread-pool/task queue etc.
 */
public abstract class Client implements IClient {
    protected IService serviceImpl;
    Looper looper;
    //Can alter to use Executor & ThreadPool, but for now suffices. Update it later.
    LinkedBlockingQueue<Task> taskQueue;
    private boolean stopConsuming;

    protected Client(){
        //By default, use main thread looper for handlers.
        this(null, Looper.getMainLooper());
    }

    /**
     * Also, possible that the service implementation is injected into the client by the API/Framework consumer/customizer.
     * This way, other components like - PersistentStore, CredentialStore etc. can also be visualized as being injected into the
     * Client facade.The good design is the one which allows the API to be used in below manners:
     * 1.Use all the default implementations: cool- no setup, no upfront config, use as it as.
     * 2.Customize: by subclassing part of the framework and/or by injecting that extended component into the system.
     * 3.Grand Overhaul: Nah! I don't like any of the things you provide man! I better build up the framework from scratch that
     * suites my needs by adhering to the base interfaces of the framework, like - IService, HaptikClient etc.
     */
    protected Client(IService serviceImpl, Looper looper){
        this.serviceImpl = serviceImpl;
        this.looper = looper;
        setupTaskQueue();
    }


    private void setupTaskQueue(){
        taskQueue = new LinkedBlockingQueue<>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!stopConsuming){
                    try {
                        Task task = taskQueue.take();//Blocks till the queue has at least one element to consume.
                        Log.i("_#_Client", "About to start executing the task....");
                        task.executeInternal();
                        Log.i("_#_Client", "Task execution done!");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public IService getService(){
        return serviceImpl;
    }

    @Override
    public Looper getLooper() {
        return looper;
    }

    public void submitTask(final Task task){
        Log.i("_#_Client", "SubmitTask");
        taskQueue.offer(task);//Non blocking, submits & retuns immediately.If queue has reached it's capacity, returns false.
    }
}
