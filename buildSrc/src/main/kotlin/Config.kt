import java.util.Calendar

/**
 * Base/Base: 基础版本。
 * Alpha: 内部测试版，Bug较多，功能不全。
 * Beta: 外部测试版，功能已基本完成，进行Beta测试。
 * RC (Release Candidate): 候选版本，已基本可发布，进行最终测试。
 * Release/GA (General Availability): 最终稳定版本。
 */
enum class Phase(
    val label: String
) {
    Alpha("alpha"),
    Beta("beta"),
    RC("rc"),
    GA("")
}

object Config {
    const val MAJOR = 1
    const val MINOR = 1
    const val PATCH = 0
    const val ROUND = 1
    val PHASE = Phase.GA

    val VERSION_CODE = getDate()
    val VERSION_NAME = getVersionName()

    const val COMPILE_SDK = 36
    const val MIN_SDK = 24
    const val TARGET_SDK = 36

    const val APP_ID = "io.github.iplanetcn.app.stopwatch"

    private fun getVersionName(): String {
        return when (PHASE) {
            Phase.GA -> "$MAJOR.$MINOR.$PATCH"
            Phase.Alpha, Phase.Beta, Phase.RC -> "$MAJOR.$MINOR.$PATCH-${PHASE.label}.${ROUND}"
        }
    }

    private fun getDate(): Int {
        val c = Calendar.getInstance()
        val y = c.get(Calendar.YEAR)
        val m = c.get(Calendar.MONTH) + 1  // 0-based
        val d = c.get(Calendar.DAY_OF_MONTH)
        return y * 10000 + m * 100 + d     // e.g. 20260509
    }
}