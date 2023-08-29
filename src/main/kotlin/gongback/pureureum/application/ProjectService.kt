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
import gongback.pureureum.domain.projectapply.ProjectApply
import gongback.pureureum.domain.projectapply.ProjectApplyRepository
import gongback.pureureum.domain.projectapply.existsByProjectAndUserId
import gongback.pureureum.domain.user.UserRepository
import gongback.pureureum.domain.user.getUserByEmail
import gongback.pureureum.domain.user.getUserById
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
    private val projectApplyRepository: ProjectApplyRepository,
    private val fileService: FileService
) {

    @Transactional
    fun registerProject(
        email: String,
        projectRegisterReq: ProjectRegisterReq,
        projectFiles: List<MultipartFile>?
    ): Long {
        val findUser = userRepository.getUserByEmail(email)

        // ProjectFileUpload
        val productFiles = projectFiles?.mapIndexed { index, multipartFile ->
            val originalFileName = fileService.validateFileName(multipartFile)
            val contentType = fileService.getAnyContentType(multipartFile)
            val fileKey = fileService.uploadFile(multipartFile, FileType.PROJECT, originalFileName)
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
        return projectRepository.save(project).id
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

        findProject.projectFiles.forEach { fileService.deleteFile(it.fileKey) }
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

    @Transactional
    fun applyProject(projectId: Long, userEmail: String) {
        val project = projectRepository.getProjectById(projectId)
        val user = userRepository.getUserByEmail(userEmail)
        val isExistedApply = projectApplyRepository.existsByProjectAndUserId(project, user.id)
        if (isExistedApply) {
            throw PureureumException(errorCode = ErrorCode.REQUEST_RESOURCE_ALREADY_EXISTS)
        }
        val projectApply = ProjectApply(user.id, project)
        projectApplyRepository.save(projectApply)
    }

    private fun projectToDto(project: Project): ProjectRes {
        val findFacility = facilityRepository.getFacilityById(project.facilityId)

        val projectFileResList = project.projectFiles.map { projectFile ->
            val projectFileUrl = fileService.getFileUrl(projectFile.fileKey)
            ProjectFileRes(projectFileUrl, projectFile.projectFileType)
        }

        return ProjectRes(project, findFacility.address, projectFileResList)
    }

    private fun convertProjectToPartRes(project: Project): ProjectPartRes {
        val findFacility = facilityRepository.getFacilityById(project.facilityId)
        val projectOwner = userRepository.getUserById(project.userId)

        return try {
            val thumbnailFile = project.projectFiles.first { it.projectFileType == ProjectFileType.THUMBNAIL }
            val thumbnailFileUrl = fileService.getFileUrl(thumbnailFile.fileKey)
            val thumbnailFileRes = ProjectFileRes(thumbnailFileUrl, thumbnailFile.projectFileType)
            ProjectPartRes(project, findFacility.address, thumbnailFileRes, projectOwner.information)
        } catch (e: NoSuchElementException) {
            ProjectPartRes(project, findFacility.address, null, projectOwner.information)
        }
    }
}
