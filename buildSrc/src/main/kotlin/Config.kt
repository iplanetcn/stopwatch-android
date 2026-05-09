import org.gradle.internal.impldep.org.joda.time.LocalDate
import org.gradle.internal.impldep.org.joda.time.format.DateTimeFormat

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
    val PHASE = Phase.Alpha

    val VERSION_CODE = getVersionCode()
    val VERSION_NAME = getVersionName()

    const val COMPILE_SDK = 36
    const val MIN_SDK = 24
    const val TARGET_SDK = 36

    const val APP_ID = "io.github.iplanetcn.app.stopwatch"

    private fun getVersionName(): String{
        return when(PHASE) {
            Phase.GA -> "$MAJOR.$MINOR.$PATCH"
            else -> "$MAJOR.$MINOR.$PATCH-${PHASE.label}.$ROUND"
        }
    }

    private fun getVersionCode(): Int {
        return when(PHASE) {
            Phase.GA -> (MAJOR * 100 + MINOR) * 100 + PATCH
            else -> ((MAJOR * 100 + MINOR) * 100 + PATCH) * 100 + ROUND
        }
    }

    private fun getDate(): String {
        val date = LocalDate.now()
        val fmt = DateTimeFormat.forPattern("yyyyMMdd")
        return fmt.print(date)
    }
}