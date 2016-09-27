package io.github.dantetam.opstrykontest;

import android.os.AsyncTask;

/**
 * Created by Dante on 9/26/2016.
 */
public class WorldHandlerTask extends AsyncTask<Void, Void, Void> {

    @Override
    protected void onPreExecute() {
        System.out.println("Starting task");
    }

    protected Void doInBackground(Void... params) {
        System.out.println("Start task");
        int i = 0;
        double sum = 0;
        while (i < 100000000) {
            i++;
            sum += i;
            sum *= 0.27391*i;
            sum = sum % 1020501;
        }
        System.out.println("End task: " + sum);
        return null;
    }

    protected void onProgressUpdate(Void... progress) {

    }

    protected void onPostExecute(Void result) {

    }

    public WorldRenderManager worldRenderManager;
    public Thread mThreadThis;
    public Runnable mRunnable;

    public void init(WorldRenderManager manager) {
        worldRenderManager = manager;
    }

    public void recycle() {

    }

}
