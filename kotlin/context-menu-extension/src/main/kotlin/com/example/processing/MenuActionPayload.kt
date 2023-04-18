package com.example.processing

import com.example.db.findRefreshTokenData
import space.jetbrains.api.runtime.*
import space.jetbrains.api.runtime.helpers.ProcessingScope
import space.jetbrains.api.runtime.resources.checklists
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
    permissionsToRequest = listOf(
        AuthCodeFlowPermissionsRequest(
            scope = PermissionScope.build(
                /**
                 * The code below requests global permissions (for all projects). You can also request permissions for a
                 * particular project only. To do that, use `ProjectPermissionContextIdentifier`:
                 * ```
                 * PermissionScopeElement(
                 *     ProjectPermissionContextIdentifier(ProjectIdentifier.Key("MY-PRJ")),
                 *     PermissionIdentifier.CreateIssues
                 *  ),
                 * ```
                 *
                 * Here replace the `MY-PRJ` with the actual project key. You can get the project key from
                 * [MenuActionPayload.context]:
                 *
                 * ```
                 * val context = payload.context as IssueMenuActionContext
                 * val projectIdentifier = context.projectIdentifier as ProjectIdentifier.Key
                 * val projectKey = projectIdentifier.key
                 * ```
                 */
                PermissionScopeElement(
                    GlobalPermissionContextIdentifier,
                    PermissionIdentifier.CreateIssues
                ),
                PermissionScopeElement(
                    GlobalPermissionContextIdentifier,
                    PermissionIdentifier.ViewIssues
                ),
                PermissionScopeElement(
                    GlobalPermissionContextIdentifier,
                    PermissionIdentifier.UpdateIssues
                ),
                PermissionScopeElement(
                    GlobalPermissionContextIdentifier,
                    PermissionIdentifier.ViewProjectDetails
                )
            ),
            purpose = "create task sub-items"
        )
    )
)

private suspend fun doCreateSubItems(client: SpaceClient, payload: MenuActionPayload) {
    val context = payload.context as IssueMenuActionContext
    val issue = client.projects.planning.issues.getIssue(context.projectIdentifier, context.issueIdentifier) {
        subItemsList {
            id()
        }
    }

    listOf(
        "Pricing plan",
        "Proofreading",
        "Marketing Materials",
        "Documentation",
        "Metrics",
        "UI tests",
    ).forEach {
        client.checklists.items.createPlanItem(
            checklist = ChecklistIdentifier.Id(issue.subItemsList.id),
            parentItem = null,
            itemText = it,
        )
    }

    /**
     * Alternatively you can create sub-issues, not sub-items.
     */
//    val status = client.projects.planning.issues.statuses.getAllIssueStatuses(context.projectIdentifier)
//        .map { it.id }.first()
//
//    listOf(
//        "Pricing plan",
//        "Proofreading",
//        "Marketing Materials",
//        "Documentation",
//        "Metrics",
//        "UI tests",
//    ).forEach {
//        client.projects.planning.issues.createIssue(
//            context.projectIdentifier,
//            title = "${issue.title}: $it",
//            status = status,
//            parents = listOf(context.issueIdentifier))
//    }
}
