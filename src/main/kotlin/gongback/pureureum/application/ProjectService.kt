package gongback.pureureum.application

import gongback.pureureum.application.dto.ErrorCode
import gongback.pureureum.application.dto.FileInfo
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
        val projectPartResList =
            projectRepository.getRunningProjectsByCategoryOrderedSearchType(type, category, pageable)
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
    private val userRepository: UserRepository,
    private val projectRepository: ProjectRepository,
    private val fileService: FileService
) {

    @Transactional
    fun registerProject(email: String, projectRegisterReq: ProjectRegisterReq, projectFiles: List<MultipartFile>?): Long {
        val findUser = userRepository.getUserByEmail(email)

        val project = projectRegisterReq.toEntityWithInfo(
            projectRegisterReq.facilityId,
            projectRegisterReq.projectCategory,
            findUser.id
        )
        return projectRepository.save(project).id
    }

    @Transactional(noRollbackFor = [FileHandlingException::class])
    fun saveProjectFiles(projectId: Long, projectFileReqs: List<MultipartFile>) {
        deleteProjectIfError({
            val projectFiles = projectFileReqs.mapIndexed { index, file ->
                val contentType = fileService.validateAnyContentType(file.contentType)
                val originalFileName = fileService.validateFileName(file.originalFilename)
                val fileInfo = FileInfo(file.size, file.inputStream, contentType, originalFileName)
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

    private fun deleteProjectIfError(operation: () -> Unit, projectId: Long) =
        runCatching {
            operation()
        }.onFailure {
            projectRepository.deleteById(projectId)
            throw FileHandlingException(it)
        }
}
