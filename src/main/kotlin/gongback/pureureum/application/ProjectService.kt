package gongback.pureureum.application

import gongback.pureureum.application.dto.ErrorCode
import gongback.pureureum.application.dto.ProjectFileRes
import gongback.pureureum.application.dto.ProjectPartPageRes
import gongback.pureureum.application.dto.ProjectPartRes
import gongback.pureureum.application.dto.ProjectRegisterReq
import gongback.pureureum.application.dto.ProjectRes
import gongback.pureureum.domain.facility.FacilityRepository
import gongback.pureureum.domain.facility.getFacilityById
import gongback.pureureum.domain.project.Project
import gongback.pureureum.domain.project.ProjectFile
import gongback.pureureum.domain.project.ProjectFileType
import gongback.pureureum.domain.project.ProjectRepository
import gongback.pureureum.domain.project.getProjectById
import gongback.pureureum.domain.user.UserRepository
import gongback.pureureum.domain.user.getUserByEmail
import gongback.pureureum.support.constant.Category
import gongback.pureureum.support.constant.FileType
import gongback.pureureum.support.constant.SearchType
import org.springframework.data.domain.Pageable
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
        val productFiles = projectFiles?.mapIndexed { index, multipartFile ->
            val originalFileName = uploadService.validateFileName(multipartFile)
            val contentType = uploadService.getAnyContentType(multipartFile)
            val fileKey = uploadService.uploadFile(multipartFile, FileType.PROJECT, originalFileName)
            when (index) {
                0 -> ProjectFile(fileKey, contentType, originalFileName, ProjectFileType.THUMBNAIL)
                else -> ProjectFile(fileKey, contentType, originalFileName)
            }
        } ?: emptyList()

        val project = projectRegisterReq.toEntityWithInfo(
            projectRegisterReq.facilityId,
            projectRegisterReq.projectCategory,
            productFiles,
            findUser.id
        )
        projectRepository.save(project)
    }

    fun getProject(id: Long): ProjectRes {
        val findProject = projectRepository.getProjectById(id)
        return projectToDto(findProject)
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

    fun getRunningProjectPartsByTypeAndCategory(
        type: SearchType,
        category: Category?,
        pageable: Pageable
    ): ProjectPartPageRes {
        val projectPartResList =
            projectRepository.getRunningProjectsByCategoryOrderedSearchType(type, category, pageable)
                .map { project -> convertProjectToPartRes(project) }

        return ProjectPartPageRes(
            pageable.pageNumber,
            projectPartResList
        )
    }

    private fun projectToDto(project: Project): ProjectRes {
        val findFacility = facilityRepository.getReferenceById(project.facilityId)

        val projectFileResList = project.projectFiles.map { projectFile ->
            val projectFileUrl = uploadService.getFileUrl(projectFile.fileKey)
            ProjectFileRes(projectFileUrl, projectFile.projectFileType)
        }

        return ProjectRes(project, findFacility.address, projectFileResList)
    }

    private fun convertProjectToPartRes(project: Project): ProjectPartRes {
        val findFacility = facilityRepository.getFacilityById(project.facilityId)

        return try {
            val thumbnailFile = project.projectFiles.first { it.projectFileType == ProjectFileType.THUMBNAIL }
            val thumbnailFileUrl = uploadService.getFileUrl(thumbnailFile.fileKey)
            val thumbnailFileRes = ProjectFileRes(thumbnailFileUrl, thumbnailFile.projectFileType)
            ProjectPartRes(project, findFacility.address, thumbnailFileRes)
        } catch (e: NoSuchElementException) {
            ProjectPartRes(project, findFacility.address, null)
        }
    }
}
