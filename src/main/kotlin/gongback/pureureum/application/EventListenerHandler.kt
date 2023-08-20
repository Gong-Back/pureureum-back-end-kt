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
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionalEventListener

@Component
class FacilityEventListenerHandler(
    private val fileService: FileService,
    private val facilityRepository: FacilityRepository
) {

    @Async
    @Transactional
    @TransactionalEventListener
    fun handleFacilityCreate(facilityCreateEvent: FacilityCreateEvent) {
        val certificationDocs = facilityCreateEvent.certificationDoc.map { file ->
            val originalFileName = fileService.validateFileName(file.originalFileName)
            val contentType = fileService.getImageType(file.contentType)
            val fileInfo = file.toFileInfo(contentType, originalFileName)
            val fileKey = fileService.uploadFile(fileInfo, FileType.FACILITY_CERTIFICATION)
            FacilityCertificationDoc(fileKey, contentType, originalFileName)
        }
        val facility = facilityRepository.getFacilityById(facilityCreateEvent.facilityId)
        facility.addCertificationDocs(certificationDocs)
    }
}

@Component
class ProjectEventListenerHandler(
    private val fileService: FileService,
    private val projectRepository: ProjectRepository
) {

    @Async
    @Transactional
    @TransactionalEventListener
    fun handleProjectCreate(projectCreateEvent: ProjectCreateEvent) {
        val projectFiles = projectCreateEvent.projectFiles.mapIndexed { index, file ->
            val originalFileName = fileService.validateFileName(file.originalFileName)
            val contentType = fileService.getAnyContentType(file.contentType)
            val fileInfo = file.toFileInfo(contentType, originalFileName)
            val fileKey = fileService.uploadFile(fileInfo, FileType.PROJECT)
            when (index) {
                0 -> ProjectFile(fileKey, contentType, originalFileName, ProjectFileType.THUMBNAIL)
                else -> ProjectFile(fileKey, contentType, originalFileName)
            }
        }
        val project = projectRepository.getProjectById(projectCreateEvent.projectId)
        project.addProjectFiles(projectFiles)
    }

    @Async
    @Transactional
    @TransactionalEventListener
    fun handleProjectDelete(projectDeleteEvent: ProjectDeleteEvent) {
        projectDeleteEvent.fileKeys.forEach {
            fileService.deleteFile(it)
        }
    }
}
