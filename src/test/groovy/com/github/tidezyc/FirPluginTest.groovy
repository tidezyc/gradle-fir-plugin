package com.github.tidezyc

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

/**
 * @author tidezyc
 */
class FirPluginTest extends GroovyTestCase {
    @Test
    public void test() {
        Project project = ProjectBuilder.builder().build()
        project.getPlugins().apply FirPlugin

        project.fir.appid = "24dda42d995d551f530003fb"
        project.fir.token = "8d970430b34f11e497ce23837fe6331e484f89e3"
        assertTrue(project.tasks.firUpload instanceof FirUploadTask)
        project.tasks.firUpload
    }
}
