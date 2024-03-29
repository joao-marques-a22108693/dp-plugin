package com.tfc.ulht.dpplugin.actions

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.tfc.ulht.dpplugin.VirtualFile
import com.tfc.ulht.dpplugin.dplib.Null

class DashboardAction : DPAction() {
    override fun perform(project: Project?) {
        val fileManager = project?.let { p -> FileEditorManager.getInstance(p) }

        val file = VirtualFile(listOf(Null()))

        ApplicationManager.getApplication().invokeLater {
            fileManager?.openFile(file, true)
        }
    }
}