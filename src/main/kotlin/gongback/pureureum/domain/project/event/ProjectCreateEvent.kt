package gongback.pureureum.domain.project.event

import gongback.pureureum.application.dto.FileReq

data class ProjectCreateEvent(
    val projectId: Long,
    val projectFiles: List<FileReq>
)
