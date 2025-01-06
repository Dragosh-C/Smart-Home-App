package app.smarthomeapp.viewmodels



import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters

class PrintMessageWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {

    val viewModel = ScenariosViewModel()

    override fun doWork(): Result {
        // This method runs in the background thread
        try {
            while (!isStopped) {
                // Print the message to Log every 10 seconds
                Log.d("PrintMessageWorker", "Message printed every 10 seconds!")
                viewModel.checkUserLocation(applicationContext)

                // sleep for 2 minutes

               Thread.sleep(120000)
            }
        } catch (e: InterruptedException) {
            // Handle interruption
            Log.e("PrintMessageWorker", "Task was interrupted", e)
        }

        // Return success when the task is finished
        return Result.success()
    }
}

