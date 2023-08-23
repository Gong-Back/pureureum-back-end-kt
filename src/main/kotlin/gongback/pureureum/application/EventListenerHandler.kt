package gongback.pureureum.application

import gongback.pureureum.domain.facility.FacilityCertificationDoc
import gongback.pureureum.domain.facility.FacilityRepository
import gongback.pureureum.domain.facility.event.FacilityCreateEvent
import gongback.pureureum.domain.facility.getFacilityById
import gongback.pureureum.domain.project.ProjectFile
import gongback.pureureum.domain.project.ProjectFileType
import gongback.pureureum.domain.project.ProjectRepository
import gongback.pureureum.domain.project.event.ProjectCreateEvent
import gongback.pureureum.domain.project.event.ProjectDeleteEvent
import gongback.pureureum.domain.project.getProjectById
import gongback.pureureum.support.constant.FileType
import gongback.pureureum.support.event.EventListenerWithTransaction
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class FacilityEventListenerHandler(
    private val fileService: FileService,
    private val facilityRepository: FacilityRepository
) {

    @Async
    @EventListenerWithTransaction
    fun handleFacilityCreate(facilityCreateEvent: FacilityCreateEvent) {
        val facilityId = facilityCreateEvent.facilityId
        deleteFacilityIfError({
            val certificationDocs = facilityCreateEvent.certificationDoc.map { file ->
                val contentType = fileService.validateImageType(file.contentType)
                val originalFileName = fileService.validateFileName(file.originalFileName)
                val fileInfo = file.toFileInfo(contentType, originalFileName)
                val fileKey = fileService.uploadFile(fileInfo, FileType.FACILITY_CERTIFICATION)
                FacilityCertificationDoc(fileKey, contentType, originalFileName)
            }
            val facility = facilityRepository.getFacilityById(facilityId)
            facility.addCertificationDocs(certificationDocs)
        }, facilityId)
    }

    private fun deleteFacilityIfError(operation: () -> Unit, facilityId: Long) =
        runCatching {
            operation()
        }.onFailure {
            facilityRepository.deleteById(facilityId)
        }
}

@Component
class ProjectEventListenerHandler(
    private val fileService: FileService,
    private val projectRepository: ProjectRepository
) {

    @Async
    @EventListenerWithTransaction
    fun handleProjectCreate(projectCreateEvent: ProjectCreateEvent) {
        val projectId = projectCreateEvent.projectId
        deleteProjectIfError({
            val projectFiles = projectCreateEvent.projectFiles.mapIndexed { index, file ->
                val contentType = fileService.validateAnyContentType(file.contentType)
                val originalFileName = fileService.validateFileName(file.originalFileName)
                val fileInfo = file.toFileInfo(contentType, originalFileName)
                val fileKey = fileService.uploadFile(fileInfo, FileType.PROJECT)
                when (index) {
                    0 -> ProjectFile(fileKey, contentType, originalFileName, ProjectFileType.THUMBNAIL)
                    else -> ProjectFile(fileKey, contentType, originalFileName)
                }
            }
            val project = projectRepository.getProjectById(projectId)
            project.addProjectFiles(projectFiles)
        }, projectId)
    }

    @Async
    @EventListenerWithTransaction
    fun handleProjectDelete(projectDeleteEvent: ProjectDeleteEvent) {
        projectDeleteEvent.fileKeys.forEach {
            fileService.deleteFile(it)
        }
    }

    private fun deleteProjectIfError(operation: () -> Unit, projectId: Long) =
        runCatching {
            operation()
        }.onFailure {
            projectRepository.deleteById(projectId)
        }
}
