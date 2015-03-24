package com.github.tidezyc

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * @author tidezyc
 */
public class FirPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.extensions.create('fir', FirExtension)
        project.tasks.create(name: 'firUpload', type: FirUploadTask, dependsOn: 'packageRelease') {

        }
        project.tasks.withType(FirUploadTask) {
            def firExt = project.extensions.getByName("fir")
            conventionMapping.appid = { project.getGroup() }
            conventionMapping.token = { firExt.token }
            conventionMapping.file = { firExt.file }
        }
    }
}
