/* eslint-disable */

export interface BasketplanGameDTO {
    gameNumber: string;
    competition: string;
    date: Date;
    result: string;
    teamA: string;
    teamB: string;
    officiatingMode: OfficiatingMode;
    referee1?: RefereeDTO;
    referee2?: RefereeDTO;
    referee3?: RefereeDTO;
    youtubeId?: string;
}

export interface ChangePasswordRequestDTO {
    oldPassword: string;
    newPassword: string;
}

export interface CoachDTO {
    id: number;
    name: string;
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

export interface CreateRepliesDTO {
    replies: CommentReplyDTO[];
    newComments: VideoCommentDTO[];
}

export interface CreateVideoReportDTO {
    federation: Federation;
    gameNumber: string;
    youtubeId?: string;
    reportee: Reportee;
}

export interface CriteriaEvaluationDTO {
    comment?: string;
    score?: number;
    rating?: string;
}

export interface LoginRequestDTO {
    email: string;
    password: string;
}

export interface LoginResponseDTO {
    id: number;
    name: string;
    admin: boolean;
    jwt: string;
}

export interface RefereeDTO {
    id: number;
    name: string;
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

export interface VideoCommentDTO {
    id?: number;
    timestamp: number;
    comment: string;
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
    coach: CoachDTO;
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
    coach: CoachDTO;
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

export enum Federation {
    SBL = "SBL",
    PROBASKET = "PROBASKET",
}
