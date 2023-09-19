import {Injectable} from '@angular/core';
import {DateTime} from "luxon";
import {Observable} from "rxjs";
import {OverviewDTO} from "../rest";
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";

@Injectable({
  providedIn: 'root'
})
export class SearchService {

  private baseUrl = environment.baseUrl;

  constructor(private readonly httpClient: HttpClient) {
  }

  find(from: DateTime, to: DateTime): Observable<OverviewDTO[]> {
    return this.httpClient.get<OverviewDTO[]>(`${this.baseUrl}/video-report?from=${from.toFormat('yyyy-MM-dd')}&to=${to.toFormat('yyyy-MM-dd')}`);
  }
}
