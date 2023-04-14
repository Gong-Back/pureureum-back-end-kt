package gongback.pureureum.application

import gongback.pureureum.application.dto.ErrorCode
import gongback.pureureum.application.dto.ProjectFileRes
import gongback.pureureum.application.dto.ProjectRegisterReq
import gongback.pureureum.application.dto.ProjectRes
import gongback.pureureum.domain.facility.FacilityRepository
import gongback.pureureum.domain.project.ProjectFile
import gongback.pureureum.domain.project.ProjectFileType
import gongback.pureureum.domain.project.ProjectRepository
import gongback.pureureum.domain.project.getProjectById
import gongback.pureureum.domain.user.UserRepository
import gongback.pureureum.domain.user.getUserByEmail
import gongback.pureureum.support.enum.FileType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
@Transactional(readOnly = true)
class ProjectService(
    private val userRepository: UserRepository,
    private val projectRepository: ProjectRepository,
    private val facilityRepository: FacilityRepository,
    private val uploadService: UploadService
) {

    @Transactional
    fun registerProject(email: String, projectRegisterReq: ProjectRegisterReq, projectFiles: List<MultipartFile>?) {
        val findUser = userRepository.getUserByEmail(email)

        // ProjectFileUpload
        val projectFileList = projectFiles?.mapIndexed { index, multipartFile ->
            val originalFileName = uploadService.validateFileName(multipartFile)
            val contentType = uploadService.getAnyContentType(multipartFile)
            val fileKey = uploadService.uploadFile(multipartFile, FileType.PROJECT, originalFileName)
            when (index) {
                0 -> ProjectFile(fileKey, contentType, originalFileName, ProjectFileType.THUMBNAIL)
                else -> ProjectFile(fileKey, contentType, originalFileName)
            }
        } ?: emptyList()

        val project = projectRegisterReq.toEntityWithInfo(projectRegisterReq.facilityId, projectFileList, findUser.id)
        projectRepository.save(project)
    }

    fun getProject(id: Long): ProjectRes {
        val findProject = projectRepository.getProjectById(id)
        val findFacility = facilityRepository.getReferenceById(findProject.facilityId)

        val projectFileResList = findProject.projectFiles.map {
            val projectFileUrl = uploadService.getFileUrl(it.fileKey)
            ProjectFileRes(projectFileUrl, it.projectFileType)
        }

        return ProjectRes(findProject, findFacility, projectFileResList)
    }

    @Transactional
    fun deleteProject(id: Long, email: String) {
        val findUser = userRepository.getUserByEmail(email)
        val findProject = projectRepository.getProjectById(id)

        if (findProject.userId != findUser.id) {
            throw PureureumException(errorCode = ErrorCode.FORBIDDEN)
        }

        findProject.projectFiles.forEach { uploadService.deleteFile(it.fileKey) }
        projectRepository.delete(findProject)
    }
}
