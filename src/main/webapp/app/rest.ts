/* eslint-disable */

export interface BasketplanGameDTO {
    gameNumber: string;
    competition: string;
    date: Date;
    result: string;
    teamA: string;
    teamB: string;
    officiatingMode: OfficiatingMode;
    referee1?: UserDTO;
    referee2?: UserDTO;
    referee3?: UserDTO;
    youtubeId?: string;
}

export interface ChangePasswordRequestDTO {
    oldPassword: string;
    newPassword: string;
}

export interface CommentReplyDTO {
    commentId: number;
    comment: string;
}

export interface CopyVideoCommentDTO {
    sourceId: number;
    reportee: Reportee;
}

export interface CopyVideoReportDTO {
    sourceId: string;
    reportee: Reportee;
}

export interface CreateGameDiscussionCommentDTO {
    replies: CommentReplyDTO[];
    newComments: GameDiscussionCommentDTO[];
}

export interface CreateGameDiscussionDTO {
    gameNumber: string;
    youtubeId: string;
}

export interface CreateRepliesDTO {
    replies: CommentReplyDTO[];
    newComments: VideoCommentDTO[];
}

export interface CreateVideoReportDTO {
    gameNumber: string;
    youtubeId?: string;
    reportee: Reportee;
}

export interface CriteriaEvaluationDTO {
    comment?: string;
    score?: number;
    rating?: string;
}

export interface ForgotPasswordRequestDTO {
    email: string;
}

export interface GameDiscussionCommentDTO {
    id?: number;
    timestamp: number;
    comment: string;
    replies: GameDiscussionCommentReplyDTO[];
}

export interface GameDiscussionCommentReplyDTO {
    id: number;
    repliedBy: string;
    repliedAt: Date;
    reply: string;
}

export interface GameDiscussionDTO {
    id: string;
    basketplanGame: BasketplanGameDTO;
    comments: GameDiscussionCommentDTO[];
}

export interface LoginRequestDTO {
    email: string;
    password: string;
}

export interface LoginResponseDTO {
    id: number;
    name: string;
    admin: boolean;
    role: UserRole;
    jwt: string;
}

export interface OverviewDTO {
    id: string;
    type: ReportType;
    date: Date;
    gameNumber: string;
    competition: string;
    teamA: string;
    teamB: string;
    coach?: UserDTO;
    reportee?: Reportee;
    relevantReferee?: UserDTO;
    referee1?: UserDTO;
    referee2?: UserDTO;
    referee3?: UserDTO;
    relevantRefereeIds: number[];
    finished: boolean;
    visibleForReferee: boolean;
    coachId?: number;
}

export interface ResetPasswordRequestDTO {
    email: string;
    token: string;
    newPassword: string;
}

export interface SearchRequestDTO {
    tags: TagDTO[];
}

export interface SearchResponseDTO {
    results: VideoCommentDetailDTO[];
}

export interface TagDTO {
    id: number;
    name: string;
}

export interface UserDTO {
    id: number;
    name: string;
    email: string;
    role: UserRole;
    admin: boolean;
}

export interface VideoCommentDTO {
    id?: number;
    timestamp: number;
    comment: string;
    requiresReply: boolean;
    replies: VideoCommentReplyDTO[];
    tags: TagDTO[];
}

export interface VideoCommentDetailDTO {
    gameNumber: string;
    competition: string;
    date: Date;
    timestamp: number;
    comment: string;
    youtubeId: string;
    tags: string;
}

export interface VideoCommentReplyDTO {
    id: number;
    repliedBy: string;
    repliedAt: Date;
    reply: string;
}

export interface VideoReportDTO {
    id: string;
    basketplanGame: BasketplanGameDTO;
    coach: UserDTO;
    reportee: Reportee;
    general: CriteriaEvaluationDTO;
    image: CriteriaEvaluationDTO;
    fitness: CriteriaEvaluationDTO;
    mechanics: CriteriaEvaluationDTO;
    fouls: CriteriaEvaluationDTO;
    violations: CriteriaEvaluationDTO;
    gameManagement: CriteriaEvaluationDTO;
    pointsToKeepComment?: string;
    pointsToImproveComment?: string;
    videoComments: VideoCommentDTO[];
    otherReportees: Reportee[];
    finished: boolean;
    version: number;
    textOnly: boolean;
}

export interface VideoReportDiscussionDTO {
    videoReportId: string;
    basketplanGame: BasketplanGameDTO;
    coach: UserDTO;
    referee: string;
    videoComments: VideoCommentDTO[];
}

export enum OfficiatingMode {
    OFFICIATING_2PO = "OFFICIATING_2PO",
    OFFICIATING_3PO = "OFFICIATING_3PO",
}

export enum Reportee {
    FIRST_REFEREE = "FIRST_REFEREE",
    SECOND_REFEREE = "SECOND_REFEREE",
    THIRD_REFEREE = "THIRD_REFEREE",
}

export enum UserRole {
    COACH = "COACH",
    REFEREE_COACH = "REFEREE_COACH",
    REFEREE = "REFEREE",
}

export enum ReportType {
    COACHING = "COACHING",
    GAME_DISCUSSION = "GAME_DISCUSSION",
}
