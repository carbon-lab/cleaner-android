package tech.sobin.cleaner

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class CleaningActivity : AppCompatActivity() {

	private var serviceIntent: Intent? = null
	private var serviceBinder: Binder? = null

	private lateinit var textLabel: TextView
	private lateinit var progressBarPercent: ProgressBar
	private lateinit var progressBarDoing: ProgressBar

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_cleaning)

		val flag = intent.getIntExtra("flag", 1)

		textLabel = findViewById(R.id.textLabel)
		progressBarPercent = findViewById(R.id.progressBarPercent)
		progressBarDoing = findViewById(R.id.progressBarDoing)

		progressBarPercent.max = 100

		serviceIntent = Intent(this, CleanService::class.java)
		serviceIntent?.putExtra("flag", flag)
		startService(serviceIntent)
		bindService(serviceIntent, object: ServiceConnection {
			override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
				if (service != null && service is TheBinder) {
					serviceBinder = service
					service.callback = object: ServiceCallback {
						override fun onStatusChanged(status: Int) {
							val msg = Message()
							val data = Bundle()
							data.putInt("status", status);
							msg.data = data
							messageHandler.sendMessage(msg)
						}

						override fun onProgressChanged(progress: Double) {
							val percent = (progress * 100).toInt()
							val msg = Message()
							val data = Bundle()
							data.putInt("progress", percent);
							msg.data = data
							messageHandler.sendMessage(msg)
						}
					}
				}
			}

			override fun onServiceDisconnected(name: ComponentName?) {
				serviceBinder = null
			}
		}, Context.BIND_AUTO_CREATE)
	}

	val messageHandler = object: Handler() {
		override fun handleMessage(msg: Message) {
			super.handleMessage(msg)
			val data = msg.data
			val status: Int = data.getInt("status", -1)
			val progress: Int = data.getInt("progress", -1)
			if (status != -1) {
				when (status) {
					1 -> { textLabel.setText(R.string.label_write_one) }
					2 -> { textLabel.setText(R.string.label_write_random) }
					3 -> {
						textLabel.setText(R.string.label_finished)
						progressBarDoing.visibility = View.INVISIBLE
					}
					else -> { textLabel.setText(R.string.label_write_zero) }
				}
			}
			if (progress != -1) {
				progressBarPercent.progress = progress
			}
		}
	}

	override fun onDestroy() {
		stopService(serviceIntent)
		serviceIntent = null
		super.finish()
		super.onDestroy()
	}

	override fun onBackPressed() {
		// Do nothing
	}
}
