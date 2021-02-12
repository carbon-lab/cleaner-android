package tech.sobin.cleaner

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.*
import java.io.IOException
import kotlin.random.Random

interface ServiceCallback {
	fun onStatusChanged(status: Int)
	fun onProgressChanged(progress: Double)
}

class TheBinder: Binder {

	private val service: CleanService

	constructor(service: CleanService) {
		this.service = service
	}

	var callback: ServiceCallback?
		get() {
			return this.service.callback
		}
		set(value) {
			this.service.callback = value
		}
}

class CleanService : Service() {

	var callback: ServiceCallback? = null

	private var zero = true
	private var one = true
	private var random = true

	private var fileCount = 0

	override fun onBind(intent: Intent): IBinder {
		return TheBinder(this)
	}

	override fun onStart(intent: Intent, startId: Int) {
		val flag = intent.getIntExtra("flag", 1)
		zero = flag.and(1) != 0
		one = flag.and(2) != 0
		random = flag.and(4) != 0

		Thread {
			var sum = 0.0
			var ct = 0.0
			if (zero) sum += 1
			if (one) sum += 1
			if (random) sum += 1

			if (zero) {
				callback?.onStatusChanged(0)
				writeBy(0)
				ct += 1
			}
			callback?.onProgressChanged(ct / sum)
			if (one) {
				callback?.onStatusChanged(1)
				writeBy(1)
				ct += 1
			}
			callback?.onProgressChanged(ct / sum)
			if (random) {
				callback?.onStatusChanged(2)
				writeBy(2)
				ct += 1
			}
			callback?.onProgressChanged(ct / sum)
			callback?.onStatusChanged(3)

			val msg = Message()
			val data = Bundle()
			data.putInt("end", 1)
			msg.data = data
			handler.sendMessage(msg)
		}.start()
	}

	private val handler = object: Handler() {
		override fun handleMessage(msg: Message) {
			val data = msg.data
			if (data.getInt("end", -1) != -1) {
				stopSelf()
			}
			super.handleMessage(msg)
		}
	}

	private fun writeBy(dataId: Int) {
		outer@
		do {
			val buffer = ByteArray(1048576)
			val ph = applicationContext.openFileOutput(
				"${Configure.PLACEHOLDER_FILENAME}_${fileCount}",
				Context.MODE_PRIVATE
			)
			fileCount += 1
			if (dataId == 0 || dataId == 1) {
				buffer.fill(dataId.toByte(), 0, buffer.size)
				for (i in 1..2048) {
					try {
						ph.write(buffer)
					} catch (ioe: IOException) {
						// End
						ph.close()
						break@outer
					}
				}
			}
			else {
				val random = Random(java.util.Date().time)
				for (i in 1..2048) {
					random.nextBytes(buffer)
					try {
						ph.write(buffer)
					} catch (ioe: IOException) {
						// End
						ph.close()
						break@outer
					}
				}
			}
			ph.close()
		} while (true)

		for (i in 0 until fileCount) {
			val fn = "${Configure.PLACEHOLDER_FILENAME}_${i}"
			try {
				applicationContext.deleteFile(fn)
			} catch (ioe: IOException) {
				// Nothing
			}
		}
	}
}
