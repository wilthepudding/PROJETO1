package com.projeto1.miditapper

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private val pickMidiCode = 101
    private var events: List<TapEvent> = emptyList()
    private var points: MutableList<KeyPoint> = mutableListOf()
    private lateinit var status: TextView
    private lateinit var speedLabel: TextView
    private var speed = 1.0f
    private val xFields = mutableMapOf<String, EditText>()
    private val yFields = mutableMapOf<String, EditText>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        points = Preferences.loadKeyPoints(this).toMutableList()
        buildUi()
    }

    private fun buildUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 28, 28, 28)
        }
        val scroll = ScrollView(this).apply { addView(root) }

        status = TextView(this).apply {
            text = "Carregue um arquivo .mid e calibre as teclas se precisar."
            textSize = 16f
            setPadding(0, 0, 0, 18)
        }
        root.addView(status)

        root.addView(button("1. Abrir permissao de acessibilidade") {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        })
        root.addView(button("2. Escolher MIDI") { chooseMidi() })

        speedLabel = TextView(this).apply { text = "Velocidade: 1.00x"; textSize = 16f }
        root.addView(speedLabel)
        root.addView(SeekBar(this).apply {
            max = 300
            progress = 100
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    speed = (progress.coerceAtLeast(25)) / 100f
                    speedLabel.text = "Velocidade: ${"%.2f".format(speed)}x"
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
                override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
            })
        })

        root.addView(button("3. Tocar em 5 segundos") { play() })
        root.addView(button("Parar") { TapAccessibilityService.stopPlayback(); toast("Parado") })

        val hint = TextView(this).apply {
            text = "Calibragem: edite X/Y olhando a posicao das teclas na tela. Os valores iniciais foram estimados pelo print."
            setPadding(0, 20, 0, 10)
        }
        root.addView(hint)
        addCalibration(root)
        root.addView(button("Salvar calibragem") { saveCalibration() })
        root.addView(button("Restaurar padrao") {
            points = Preferences.defaultKeyPoints(this).toMutableList()
            Preferences.saveKeyPoints(this, points)
            buildUi()
        })
        setContentView(scroll)
    }

    private fun addCalibration(root: LinearLayout) {
        points.forEach { p ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, 4, 0, 4)
            }
            row.addView(TextView(this).apply { text = p.key; textSize = 18f }, LinearLayout.LayoutParams(70, ViewGroup.LayoutParams.WRAP_CONTENT))
            val x = EditText(this).apply { setText(p.x.toString()); inputType = 2 }
            val y = EditText(this).apply { setText(p.y.toString()); inputType = 2 }
            xFields[p.key] = x
            yFields[p.key] = y
            row.addView(TextView(this).apply { text = "X" })
            row.addView(x, LinearLayout.LayoutParams(180, ViewGroup.LayoutParams.WRAP_CONTENT))
            row.addView(TextView(this).apply { text = "Y" })
            row.addView(y, LinearLayout.LayoutParams(180, ViewGroup.LayoutParams.WRAP_CONTENT))
            root.addView(row)
        }
    }

    private fun button(text: String, action: () -> Unit): Button = Button(this).apply {
        this.text = text
        setOnClickListener { action() }
    }

    private fun chooseMidi() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        startActivityForResult(intent, pickMidiCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == pickMidiCode && resultCode == RESULT_OK) {
            val uri: Uri = data?.data ?: return
            val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return
            events = MidiParser.parse(bytes)
            status.text = "MIDI carregado: ${events.size} notas convertidas."
            if (events.isEmpty()) toast("Nao encontrei notas compativeis nesse MIDI")
        }
    }

    private fun saveCalibration() {
        points = KeyMapper.defaultKeys.map { key ->
            KeyPoint(
                key,
                xFields[key]?.text?.toString()?.toIntOrNull() ?: 0,
                yFields[key]?.text?.toString()?.toIntOrNull() ?: 0
            )
        }.toMutableList()
        Preferences.saveKeyPoints(this, points)
        toast("Calibragem salva")
    }

    private fun play() {
        saveCalibration()
        if (events.isEmpty()) {
            toast("Escolha um MIDI primeiro")
            return
        }
        if (!TapAccessibilityService.isReady()) {
            toast("Ative o servico Projeto1 MIDI Tapper em Acessibilidade")
            return
        }
        val ok = TapAccessibilityService.play(events, points, speed, 5000)
        if (ok) toast("Vai tocar em 5 segundos. Abra o piano agora.")
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
}
