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

export interface CopyVideoReportDTO {
    sourceId: string;
    reportee: Reportee;
}

export interface CreateVideoReportDTO {
    federation: Federation;
    gameNumber: string;
    youtubeId: string;
    reportee: Reportee;
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

export interface ReporterDTO {
    id: number;
    name: string;
}

export interface UserDTO {
    email?: string;
    admin: boolean;
}

export interface VideoCommentDTO {
    timestamp: number;
    comment: string;
}

export interface VideoReportDTO {
    id: string;
    basketplanGame: BasketplanGameDTO;
    reporter: ReporterDTO;
    reportee: Reportee;
    imageComment?: string;
    mechanicsComment?: string;
    foulsComment?: string;
    gameManagementComment?: string;
    pointsToKeepComment?: string;
    pointsToImproveComment?: string;
    videoComments: VideoCommentDTO[];
    finished: boolean;
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
