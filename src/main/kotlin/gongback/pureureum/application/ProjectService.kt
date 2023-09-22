package gongback.pureureum.application

import gongback.pureureum.application.dto.ErrorCode
import gongback.pureureum.application.dto.FileDto
import gongback.pureureum.application.dto.ProjectFileRes
import gongback.pureureum.application.dto.ProjectPartPageRes
import gongback.pureureum.application.dto.ProjectPartRes
import gongback.pureureum.application.dto.ProjectRegisterReq
import gongback.pureureum.application.dto.ProjectRes
import gongback.pureureum.application.dto.ProjectfileDto
import gongback.pureureum.application.util.FileErrorHandler
import gongback.pureureum.domain.facility.FacilityRepository
import gongback.pureureum.domain.facility.getFacilityById
import gongback.pureureum.domain.project.Project
import gongback.pureureum.domain.project.ProjectFileType
import gongback.pureureum.domain.project.ProjectRepository
import gongback.pureureum.domain.project.getProjectById
import gongback.pureureum.domain.projectapply.ProjectApply
import gongback.pureureum.domain.projectapply.ProjectApplyRepository
import gongback.pureureum.domain.projectapply.existsByProjectIdAndUserId
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
import java.time.LocalDate

@Service
@Transactional(readOnly = true)
class ProjectReadService(
    private val fileService: FileService,
    private val userRepository: UserRepository,
    private val projectRepository: ProjectRepository,
    private val facilityRepository: FacilityRepository
) {

    fun getProject(id: Long): ProjectRes {
        val findProject = projectRepository.getProjectById(id)
        return projectToDto(findProject)
    }

    fun getRunningProjectPartsByTypeAndCategory(
        type: SearchType,
        category: Category?,
        pageable: Pageable
    ): ProjectPartPageRes {
        val currentDate = LocalDate.now()
        val projectPartResList =
            projectRepository.getRunningProjectsByCategoryOrderedSearchType(type, category, pageable, currentDate)
                .map { project -> convertProjectToPartRes(project) }

        return ProjectPartPageRes(
            pageable.pageNumber,
            projectPartResList
        )
    }

    private fun projectToDto(project: Project): ProjectRes {
        val findFacility = facilityRepository.getFacilityById(project.facilityId)

        val projectFileResList = project.projectFiles.map { projectFile ->
            val projectFileUrl = fileService.getFileUrl(projectFile.fileKey)
            ProjectFileRes(projectFileUrl, projectFile.projectFileType)
        }

        val projectOwner = userRepository.getUserById(project.userId)
        return ProjectRes(project, findFacility.address, projectFileResList, projectOwner.information)
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

@Service
class ProjectWriteService(
    private val fileService: FileService,
    private val userRepository: UserRepository,
    private val projectRepository: ProjectRepository,
    private val projectApplyRepository: ProjectApplyRepository
) {

    @Transactional
    fun registerProject(email: String, projectRegisterReq: ProjectRegisterReq): Long {
        val findUser = userRepository.getUserByEmail(email)

        val project = projectRegisterReq.toEntityWithInfo(
            projectRegisterReq.facilityId,
            projectRegisterReq.projectCategory,
            findUser.id
        )
        return projectRepository.save(project).id
    }

    fun uploadProjectFiles(projectFileReqs: List<MultipartFile>): List<ProjectfileDto> =
        FileErrorHandler.throwFileHandlingExceptionIfFail {
            projectFileReqs.mapIndexed { index, file ->
                val contentType = fileService.validateAnyContentType(file.contentType)
                val originalFileName = fileService.validateFileName(file.originalFilename)
                val fileDto = FileDto(file.size, file.inputStream, contentType, originalFileName)
                val fileKey = fileService.uploadFile(fileDto, FileType.PROJECT)
                when (index) {
                    0 -> ProjectfileDto(fileKey, contentType, originalFileName, ProjectFileType.THUMBNAIL)
                    else -> ProjectfileDto(fileKey, contentType, originalFileName, ProjectFileType.COMMON)
                }
            }
        }

    @Transactional
    fun saveProjectFiles(projectId: Long, projectFileDtos: List<ProjectfileDto>) {
        val project = projectRepository.getProjectById(projectId)
        val projectFiles = projectFileDtos.map(ProjectfileDto::toEntity)
        project.addProjectFiles(projectFiles)
    }

    @Transactional
    fun deleteProject(projectId: Long) {
        projectRepository.deleteById(projectId)
    }

    @Transactional
    fun deleteProject(id: Long, email: String): List<String> {
        val findUser = userRepository.getUserByEmail(email)
        val findProject = projectRepository.getProjectById(id)

        if (findProject.userId != findUser.id) {
            throw PureureumException(errorCode = ErrorCode.FORBIDDEN)
        }
        projectRepository.delete(findProject)
        return findProject.projectFiles.map {
            it.fileKey
        }
    }

    fun deleteProjectFiles(projectFileKeys: List<String>) {
        projectFileKeys.forEach {
            fileService.deleteFile(it)
        }
    }

    @Transactional
    fun applyProject(projectId: Long, userEmail: String) {
        val project = projectRepository.getProjectById(projectId)
        val user = userRepository.getUserByEmail(userEmail)
        val isExistedApply = projectApplyRepository.existsByProjectIdAndUserId(project.id, user.id)
        if (isExistedApply) {
            throw PureureumException(errorCode = ErrorCode.REQUEST_RESOURCE_ALREADY_EXISTS)
        }
        val projectApply = ProjectApply(project.id, user.id)
        projectApplyRepository.save(projectApply)
    }
}
