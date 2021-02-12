package tech.sobin.cleaner

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox

class MainActivity : AppCompatActivity() {

	private lateinit var btnStart: Button
	private lateinit var checkZero: CheckBox
	private lateinit var checkOne: CheckBox
	private lateinit var checkRandom: CheckBox

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		btnStart = findViewById(R.id.btnStart)
		checkZero = findViewById(R.id.checkZero)
		checkOne = findViewById(R.id.checkOne)
		checkRandom = findViewById(R.id.checkRandom)

		btnStart.setOnClickListener {
			var flag: Int = 1
			checkZero.isClickable = false
			checkOne.isClickable = false
			checkRandom.isClickable = false
			if (checkZero.isChecked) flag = flag.or(1)
			if (checkOne.isChecked) flag = flag.or(2)
			if (checkRandom.isChecked) flag = flag.or(4)
			if (flag == 0) {
				flag = 1
				checkZero.isChecked = true
				checkOne.isChecked = false
				checkRandom.isChecked = false
			}
			val intent = Intent(this, CleaningActivity::class.java)
			intent.putExtra("flag", flag)
			startActivity(intent)
			finish()
		}
	}
}
