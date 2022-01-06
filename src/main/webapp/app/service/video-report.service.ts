import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {CopyVideoReportDTO, CreateVideoReportDTO, Federation, Reportee, VideoReportDTO} from "../rest";
import {environment} from "../../environments/environment";

export function getReferee(report: VideoReportDTO): string {
    switch (report.reportee) {
        case Reportee.FIRST_REFEREE:
            return report.basketplanGame.referee1!.name;
        case Reportee.SECOND_REFEREE:
            return report.basketplanGame.referee2!.name;
        case Reportee.THIRD_REFEREE:
            return report.basketplanGame.referee3!.name;
    }
}

@Injectable({
    providedIn: 'root'
})
export class VideoReportService {

    private baseUrl = environment.baseUrl;

    constructor(private readonly httpClient: HttpClient) {
    }

    getVideoReport(id: string): Observable<VideoReportDTO> {
        return this.httpClient.get<VideoReportDTO>(`${this.baseUrl}/video-report/${id}`);
    }

    getAllVideoReports(): Observable<VideoReportDTO[]> {
        return this.httpClient.get<VideoReportDTO[]>(`${this.baseUrl}/video-report`);
    }

    createVideoReport(gameNumber: string, youtubeId: string, reportee: Reportee): Observable<VideoReportDTO> {
        const request: CreateVideoReportDTO = {
            gameNumber: gameNumber,
            youtubeId: youtubeId,
            reportee: reportee,
            federation: Federation.SBL
        };
        return this.httpClient.post<VideoReportDTO>(`${this.baseUrl}/video-report`, request);
    }

    copyVideoReport(sourceId: string, reportee: Reportee) {
        const request: CopyVideoReportDTO = {
            sourceId: sourceId,
            reportee: reportee,
        };
        return this.httpClient.post<VideoReportDTO>(`${this.baseUrl}/video-report/copy`, request)
    }

    saveVideoReport(dto: VideoReportDTO): Observable<VideoReportDTO> {
        return this.httpClient.put<VideoReportDTO>(`${this.baseUrl}/video-report/${dto.id}`, dto);
    }

    deleteVideoReport(dto: VideoReportDTO): Observable<any> {
        return this.httpClient.delete<any>(`${this.baseUrl}/video-report/${dto.id}`);
    }

}
