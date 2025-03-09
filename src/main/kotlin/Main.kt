package org.declarations

import java.io.File

fun main(args: Array<String>) {
    if (args.isEmpty() || args.size != 1) {
        println("Usage: ./public <file-name>")
        return
    }

    val baseName = args[0]
    val baseFile = File(baseName)
    File(baseName).walkTopDown().forEach { file ->
        KtUtils.processKotlinFile(file, baseFile)
    }
}
