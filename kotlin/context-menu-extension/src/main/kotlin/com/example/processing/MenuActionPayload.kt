package com.example.processing

import com.example.db.findRefreshTokenData
import space.jetbrains.api.runtime.PermissionDeniedException
import space.jetbrains.api.runtime.RefreshTokenRevokedException
import space.jetbrains.api.runtime.SpaceClient
import space.jetbrains.api.runtime.helpers.ProcessingScope
import space.jetbrains.api.runtime.resources.projects
import space.jetbrains.api.runtime.types.*

suspend fun ProcessingScope.createIssueSubItems(payload: MenuActionPayload): AppUserActionExecutionResult {
    val refreshTokenAndScope = findRefreshTokenData(payload.clientId, payload.userId)
        ?: return permissionsRequest

    val client = clientWithRefreshToken(refreshTokenAndScope.refreshToken, refreshTokenAndScope.scope)
    return try {
        doCreateSubItems(client, payload)
        AppUserActionExecutionResult.Success(null)
    } catch (e: PermissionDeniedException) {
        permissionsRequest
    } catch (e: RefreshTokenRevokedException) {
        permissionsRequest
    }
}

private val permissionsRequest = AppUserActionExecutionResult.AuthCodeFlowRequired(
    /**
     * Below global permissions are requested (for all projects). You can also request permissions for a
     * particular project only. To do that, specify the following scope:
     * project:<project_id>:Project.Issues.Create project:<project_id>:Project.Issues.View project:<project_id>:Project.Issues.Edit
     *
     * Here replace the `<project_id>` with the id of the actual project. You can get the project id from
     * [MenuActionPayload.context]:
     *
     * ```
     * val context = payload.context as IssueMenuActionContext
     * val projectIdentifier = context.projectIdentifier as ProjectIdentifier.Id
     * val projectId = projectIdentifier.id
     * ```
     */
    listOf(
        AuthCodeFlowPermissionsRequest(
            "global:Project.Issues.Create global:Project.Issues.View global:Project.Issues.Edit",
            "create task sub-items"
        )
    )
)

private suspend fun doCreateSubItems(client: SpaceClient, payload: MenuActionPayload) {
    val context = payload.context as IssueMenuActionContext
    val issue = client.projects.planning.issues.getIssue(context.projectIdentifier, context.issueIdentifier)

    val status = client.projects.planning.issues.statuses.getAllIssueStatuses(context.projectIdentifier)
        .map { it.id }.first()

    listOf(
        "Pricing plan",
        "Proofreading",
        "Marketing Materials",
        "Documentation",
        "Metrics",
        "UI tests",
    ).forEach {
        client.projects.planning.issues.createIssue(
            context.projectIdentifier,
            title = "${issue.title}: $it",
            status = status,
            parents = listOf(context.issueIdentifier)
        )
    }
}
