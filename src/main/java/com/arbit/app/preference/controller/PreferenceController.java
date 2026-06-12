package com.arbit.app.preference.controller;

import com.arbit.app.auth.security.CustomUserDetails;
import com.arbit.app.common.response.ApiResponse;
import com.arbit.app.preference.dto.PreferenceCategoriesResponse;
import com.arbit.app.preference.service.PreferenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "취향 입력", description = "APIs for choosing categories and taste keywords.")
public class PreferenceController {

    private final PreferenceService preferenceService;

    public PreferenceController(PreferenceService preferenceService) {
        this.preferenceService = preferenceService;
    }

    @GetMapping("/api/preferences/categories")
    @Operation(
            summary = "Get seed events for choosing preferences",
            description = "Generates a random state, calls the seed-event service, and returns 20 events with event_id, title, genre, and posterImage.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Exactly 20 seed events retrieved successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = PreferenceCategoriesApiResponse.class),
                                    examples = @ExampleObject(
                                            value = """
                                                    {
                                                      "success": true,
                                                      "data": [
                                                        {
                                                          "event_id": "b6a12eff-4e34-48aa-ae09-c4152b059462",
                                                          "title": "(사)서울윈드오케스트라 제 119회 정기연주회 [윈드 오케스트라로 만나는 국악음악]",
                                                          "genre": "콘서트",
                                                          "posterImage": "https://culture.seoul.go.kr/cmmn/file/getImage.do?atchFileId=42c9b504a45f439bbaabc86a6bd68dbe&thumb=Y"
                                                        },
                                                        {
                                                          "event_id": "8dce5e8e-f880-419d-8dbf-604dc042dd83",
                                                          "title": "[DEMILE Solo Exhibition] 사이-음",
                                                          "genre": "전시/미술",
                                                          "posterImage": "https://culture.seoul.go.kr/cmmn/file/getImage.do?atchFileId=a11e3940a8d749b19911f3fdaa48abe4&thumb=Y"
                                                        },
                                                        {
                                                          "event_id": "f7bb332d-d687-4240-86b5-7c6099f8bb48",
                                                          "title": "[GS아트센터] NDT 1 - 필름 스크리닝 [미스트]",
                                                          "genre": "무용",
                                                          "posterImage": "https://culture.seoul.go.kr/cmmn/file/getImage.do?atchFileId=d3b7aaac258d4d63b74178084bc4bb88&thumb=Y"
                                                        },
                                                        {
                                                          "event_id": "9611442c-de91-4cd8-b0be-edaf5345a0e8",
                                                          "title": "[GS아트센터] 다미앵 잘레 X 코헤이 나와 [프리즘]",
                                                          "genre": "무용",
                                                          "posterImage": "https://culture.seoul.go.kr/cmmn/file/getImage.do?atchFileId=7ad9d266674546dfa5fcdeb47fc42b02&thumb=Y"
                                                        },
                                                        {
                                                          "event_id": "bd98296b-c9ca-4dbe-b3d1-35df5ee014a3",
                                                          "title": "[GS아트센터] 양인모 X 김치앤칩스",
                                                          "genre": "클래식",
                                                          "posterImage": "https://culture.seoul.go.kr/cmmn/file/getImage.do?atchFileId=344dc2e128c94b82977c28aab4a1381d&thumb=Y"
                                                        },
                                                        {
                                                          "event_id": "32355cd5-3c60-42ac-9089-97f03f2e0db5",
                                                          "title": "[강남구 평생학습센터] 유럽 4개국 문화원과 함께하는 [세계인의날 특강]",
                                                          "genre": "교육/체험",
                                                          "posterImage": "https://culture.seoul.go.kr/cmmn/file/getImage.do?atchFileId=2b051a3616dc45fbbab4b26e6fe7d03b&thumb=Y"
                                                        },
                                                        {
                                                          "event_id": "f8ad19ee-f72c-47fa-b967-1d23c3d0e781",
                                                          "title": "[강남문화재단] 5월 키즈예술공연 [숲속 동물들의 재주잔치]",
                                                          "genre": "연극",
                                                          "posterImage": "https://culture.seoul.go.kr/cmmn/file/getImage.do?atchFileId=d1d60ce6a7ef4c6098d6cc6edf4b2b70&thumb=Y"
                                                        },
                                                        {
                                                          "event_id": "41fff40a-2020-4b91-92df-382099dc9005",
                                                          "title": "[강남문화재단] 배리어프리 공연 넌버벌 [네네네]",
                                                          "genre": "연극",
                                                          "posterImage": "https://culture.seoul.go.kr/cmmn/file/getImage.do?atchFileId=221d93a6fd7745b68e135eb1ca3827bb&thumb=Y"
                                                        },
                                                        {
                                                          "event_id": "56157af5-32de-4e5f-9e13-f00e9c33532d",
                                                          "title": "[강남문화재단] 제1012회 목요예술무대 [다크니스 품바]",
                                                          "genre": "무용",
                                                          "posterImage": "https://culture.seoul.go.kr/cmmn/file/getImage.do?atchFileId=a7677f5d2e154b0f8b452ea8c70faa77&thumb=Y"
                                                        },
                                                        {
                                                          "event_id": "0aefde13-9b9f-46d3-8ab7-9d9c37764baa",
                                                          "title": "[강남힐링센터] 힐링 명사와의 특별한 하루 [이향란의 운동습관 특강]",
                                                          "genre": "교육/체험",
                                                          "posterImage": "https://culture.seoul.go.kr/cmmn/file/getImage.do?atchFileId=bab479e76786421b92db03a9525342b1&thumb=Y"
                                                        },
                                                        {
                                                          "event_id": "1cc2aadf-c4e6-4691-aa9e-d72d027dfb83",
                                                          "title": "[강동문화재단] 밀레니엄심포니오케스트라 [지브리&디즈니 OST 콘서트]",
                                                          "genre": "클래식",
                                                          "posterImage": "https://culture.seoul.go.kr/cmmn/file/getImage.do?atchFileId=6d4f53a85554411fab356230b5c14711&thumb=Y"
                                                        },
                                                        {
                                                          "event_id": "309cee43-f49b-4706-9911-cc9ee204b0f8",
                                                          "title": "[강동문화재단] 서울시무용단 [미메시스]",
                                                          "genre": "무용",
                                                          "posterImage": "https://culture.seoul.go.kr/cmmn/file/getImage.do?atchFileId=34dbcae7b24b434a9846cd9c30cba4bb&thumb=Y"
                                                        },
                                                        {
                                                          "event_id": "85d61140-a5d0-46a6-8bea-1b03c748b38c",
                                                          "title": "[강동문화재단] 쥬세뻬 비탈레 [동물의 세계: 몬도 아니말레]",
                                                          "genre": "전시/미술",
                                                          "posterImage": "https://culture.seoul.go.kr/cmmn/file/getImage.do?atchFileId=3474ac29ece741f89ccba60d785609e1&thumb=Y"
                                                        },
                                                        {
                                                          "event_id": "0dd3366f-9005-42e5-b91f-8545efb4605a",
                                                          "title": "[강동문화재단] 판소리 음악극 [긴긴밤]",
                                                          "genre": "국악",
                                                          "posterImage": "https://culture.seoul.go.kr/cmmn/file/getImage.do?atchFileId=7a04e39dc20747a5b2feefa02e3731ca&thumb=Y"
                                                        },
                                                        {
                                                          "event_id": "51874106-fcc4-4b23-9ec3-c456f67aa26d",
                                                          "title": "[강북문화재단] 2026 [419연극제] 극단 76 [기국서의 햄릿]",
                                                          "genre": "연극",
                                                          "posterImage": "https://culture.seoul.go.kr/cmmn/file/getImage.do?atchFileId=920e7a18932e469eaa87a03fa5f1017e&thumb=Y"
                                                        },
                                                        {
                                                          "event_id": "bd496fc3-717a-47c1-b6e6-0fdda8c3b139",
                                                          "title": "[강북문화재단] 2026 마티네 콘서트 [탱고가 흐르는 아침]",
                                                          "genre": "콘서트",
                                                          "posterImage": "https://culture.seoul.go.kr/cmmn/file/getImage.do?atchFileId=470dce4252fa4f3dad45ea34323a2c34&thumb=Y"
                                                        },
                                                        {
                                                          "event_id": "41bff7df-b85e-46ea-96d4-d76811365ac4",
                                                          "title": "[강북문화재단] The Grand Show! 김연자x나태주x조엘라",
                                                          "genre": "콘서트",
                                                          "posterImage": "https://culture.seoul.go.kr/cmmn/file/getImage.do?atchFileId=9b5727f91e7c402b931a9547a50fe2b9&thumb=Y"
                                                        },
                                                        {
                                                          "event_id": "53b0214c-38dd-4c88-b7d6-a922e28f29ad",
                                                          "title": "[갤러리지우헌] 허상욱 개인전: 분청 마음",
                                                          "genre": "전시/미술",
                                                          "posterImage": "https://culture.seoul.go.kr/cmmn/file/getImage.do?atchFileId=cbdadfe6623e471d8e41a958828bab71&thumb=Y"
                                                        },
                                                        {
                                                          "event_id": "e7154bb0-b95d-421c-a2d6-f3d6b0aadee3",
                                                          "title": "[거마도서관] 상주작가 지금 몇 시? 거마시(詩) : 최필립 작가와의 만남",
                                                          "genre": "교육/체험",
                                                          "posterImage": "https://culture.seoul.go.kr/cmmn/file/getImage.do?atchFileId=abfcaf3658264c52bab778e5255e45ad&thumb=Y"
                                                        },
                                                        {
                                                          "event_id": "b9a6b1cb-e13b-49f7-bd29-b234bdf9609c",
                                                          "title": "[관악문화재단] 2026 S1472 어린이주간 [미술로 봄!]",
                                                          "genre": "축제-기타",
                                                          "posterImage": "https://culture.seoul.go.kr/cmmn/file/getImage.do?atchFileId=e2abe55ed3e34e39a3d7104bae940e86&thumb=Y"
                                                        },
                                                        {
                                                          "event_id": "415f0e93-34d4-4589-9193-28720fd7ff0e",
                                                          "title": "[관악문화재단] 2026 관악 책빵축제",
                                                          "genre": "축제-문화/예술",
                                                          "posterImage": "https://culture.seoul.go.kr/cmmn/file/getImage.do?atchFileId=2b23e4cb3ba54768bd2047d5e726eadc&thumb=Y"
                                                        },
                                                        {
                                                          "event_id": "3e72c275-e7d7-48ae-a83d-bef44efa6511",
                                                          "title": "[관악문화재단] 음악공장 노올량 [플레이리스트 : 세대별 아리랑]",
                                                          "genre": "국악",
                                                          "posterImage": "https://culture.seoul.go.kr/cmmn/file/getImage.do?atchFileId=264e220c83bc49a292d54dd9adde57ef&thumb=Y"
                                                        },
                                                        {
                                                          "event_id": "d81ac211-dd61-4552-aba3-b3c717503592",
                                                          "title": "[관악문화재단] 제5회 관악별빛사생대회",
                                                          "genre": "축제-기타",
                                                          "posterImage": "https://culture.seoul.go.kr/cmmn/file/getImage.do?atchFileId=b1ac0f0aceaf403791d37c4de0443a45&thumb=Y"
                                                        },
                                                        {
                                                          "event_id": "5e3b6112-b31f-433f-aeb3-0769c4f813ec",
                                                          "title": "[관학문화재단] 로열인문학 [윤일상의 창작 노트]를 훔치다]",
                                                          "genre": "콘서트",
                                                          "posterImage": "https://culture.seoul.go.kr/cmmn/file/getImage.do?atchFileId=e6c55178c2334be09b6601f19b229cb2&thumb=Y"
                                                        },
                                                        {
                                                          "event_id": "0b7be4e7-abc4-447c-bc4a-8b4e23e516fe",
                                                          "title": "[관학문화재단] 어린이뮤지컬 [피터래빗]",
                                                          "genre": "뮤지컬/오페라",
                                                          "posterImage": "https://culture.seoul.go.kr/cmmn/file/getImage.do?atchFileId=cea355cb36264547ad53c2f049d4304b&thumb=Y"
                                                        },
                                                        {
                                                          "event_id": "fcc962cb-885d-40fa-9b30-26ffbb90bb7f",
                                                          "title": "[광대생각] 2026 유아문화예술교육 [나를 데려다줘!] 참여 유아교육기관 모집",
                                                          "genre": "교육/체험",
                                                          "posterImage": "https://culture.seoul.go.kr/cmmn/file/getImage.do?atchFileId=43af3aba047a47359aa00e81f103a36a&thumb=Y"
                                                        },
                                                        {
                                                          "event_id": "1c44be12-c752-4f8c-83c5-8e606f3c6a61",
                                                          "title": "[광진문화재단] 2026 나루동요제",
                                                          "genre": "축제-기타",
                                                          "posterImage": "https://culture.seoul.go.kr/cmmn/file/getImage.do?atchFileId=7c38e0720f5f4e9b89970006fc1868e2&thumb=Y"
                                                        },
                                                        {
                                                          "event_id": "bda18246-5707-4596-afd2-cbd745422fc6",
                                                          "title": "[광진문화재단] 2026 피크닉 in 나루",
                                                          "genre": "축제-문화/예술",
                                                          "posterImage": "https://culture.seoul.go.kr/cmmn/file/getImage.do?atchFileId=afe44802e74a4537b78b5f233453242b&thumb=Y"
                                                        },
                                                        {
                                                          "event_id": "06d24430-f141-4d3a-a1be-712c31510269",
                                                          "title": "[광진문화재단] 나루아티스트와 함께하는 '문화가 있는 날' [살롱음악회] (6월)",
                                                          "genre": "클래식",
                                                          "posterImage": "https://culture.seoul.go.kr/cmmn/file/getImage.do?atchFileId=da99716a20254f859683ad3fe513c7b0&thumb=Y"
                                                        },
                                                        {
                                                          "event_id": "f4831f2f-ca94-477e-ad9a-b48b450ebac4",
                                                          "title": "[광진문화재단] 제26회 서울국제즉흥춤축제 즉흥피크닉(폐막식)",
                                                          "genre": "무용",
                                                          "posterImage": "https://culture.seoul.go.kr/cmmn/file/getImage.do?atchFileId=90a03fc6dbc0410d9d59155685d39628&thumb=Y"
                                                        },
                                                        {
                                                          "event_id": "7c3dbca7-43fa-4a1e-b730-e57417a46788",
                                                          "title": "[구로문화누리도서관] 북토크 [경쟁 교육은 야만이다]",
                                                          "genre": "교육/체험",
                                                          "posterImage": "https://culture.seoul.go.kr/cmmn/file/getImage.do?atchFileId=370f364d37be49e6af8f9e37966e87fd&thumb=Y"
                                                        },
                                                        {
                                                          "event_id": "efc2dbce-1080-4e43-a544-c7c61f706d86",
                                                          "title": "[구로문화재단] 2026 이달의 공연 [사운드트립]_5월 조환지&정승원",
                                                          "genre": "뮤지컬/오페라",
                                                          "posterImage": "https://culture.seoul.go.kr/cmmn/file/getImage.do?atchFileId=8bb6dd395dad462789e510df32fde948&thumb=Y"
                                                        },
                                                        {
                                                          "event_id": "92925316-4f38-4ccc-978e-ce5aac0834c8",
                                                          "title": "[구로문화재단] 2026 이달의 공연 [사운드트립]_6월 최정원&드림뮤지컬",
                                                          "genre": "뮤지컬/오페라",
                                                          "posterImage": "https://culture.seoul.go.kr/cmmn/file/getImage.do?atchFileId=93a4ffe561c7423d82e781ef1343664c&thumb=Y"
                                                        },
                                                        {
                                                          "event_id": "fe7e9d2c-8f19-4118-bd93-97adebbd57a1",
                                                          "title": "[구로문화재단] 오류아트홀 5월기획공연 [춤이 말하다; 문소리X리아킴]",
                                                          "genre": "무용",
                                                          "posterImage": "https://culture.seoul.go.kr/cmmn/file/getImage.do?atchFileId=8cc238910c694a388c6673bceb238d69&thumb=Y"
                                                        },
                                                        {
                                                          "event_id": "7e07f112-b8a5-4df5-bd4a-500b6d39c6c2",
                                                          "title": "[구로문화재단] 퓨전국악 콘서트_6월의 퀸(Queen)",
                                                          "genre": "국악",
                                                          "posterImage": "https://culture.seoul.go.kr/cmmn/file/getImage.do?atchFileId=40a19a1f88b944a4a75f7f8fd0f58053&thumb=Y"
                                                        },
                                                        {
                                                          "event_id": "dd428077-8a17-4edd-a2ce-accf34680833",
                                                          "title": "[구립증산도서관] 2026 부처님 오신 날 맞이 [문화 체험 DAY]",
                                                          "genre": "교육/체험",
                                                          "posterImage": "https://culture.seoul.go.kr/cmmn/file/getImage.do?atchFileId=04b2709734d849afb5c22cccf75b0abd&thumb=Y"
                                                        },
                                                        {
                                                          "event_id": "030f7321-bfe5-4d34-a59f-e2f2a71468ed",
                                                          "title": "[구립증산도서관] 가정의 달 맞이 [나도 건축가! : 네모네모 집 만들기]",
                                                          "genre": "교육/체험",
                                                          "posterImage": "https://culture.seoul.go.kr/cmmn/file/getImage.do?atchFileId=b9303e0496b24a388b349b1f0781756a&thumb=Y"
                                                        },
                                                        {
                                                          "event_id": "73589d78-7d5c-4c64-a6db-cfbb188dc482",
                                                          "title": "[구립증산도서관] 가정의 달 맞이 로비 이벤트 [우리 가족 얼마나 행복할까]",
                                                          "genre": "교육/체험",
                                                          "posterImage": "https://culture.seoul.go.kr/cmmn/file/getImage.do?atchFileId=d8890dc17cb648c5b17979b77ba76e65&thumb=Y"
                                                        },
                                                        {
                                                          "event_id": "d5f473dc-f0bd-466f-bcc6-1c290cf5d163",
                                                          "title": "[구립증산도서관] 가정의 달 맞이 형제자매 협동 프로그램 [내 옆에 쏙, 평생 친구!]",
                                                          "genre": "교육/체험",
                                                          "posterImage": "https://culture.seoul.go.kr/cmmn/file/getImage.do?atchFileId=1fd7ed59de5e4d10b750db1988cd57b1&thumb=Y"
                                                        },
                                                        {
                                                          "event_id": "3851e246-a4b5-4c51-82fb-b589cecbd8c3",
                                                          "title": "[구립증산도서관] 가정의 달 특별 베이킹드로잉(슈링클스) 원데이클래스 [우리 가족 액자 만들기]",
                                                          "genre": "교육/체험",
                                                          "posterImage": "https://culture.seoul.go.kr/cmmn/file/getImage.do?atchFileId=23a61bf9314741fba80742f57bcdae14&thumb=Y"
                                                        },
                                                        {
                                                          "event_id": "e7fce116-dbb2-4634-b9f7-50646bd46a94",
                                                          "title": "[구립증산도서관] 가정의 달 특별공연 [마술연필을 가진 마술사]",
                                                          "genre": "기타",
                                                          "posterImage": "https://culture.seoul.go.kr/cmmn/file/getImage.do?atchFileId=dcd27fbc78af41268ac7c2f267ac1513&thumb=Y"
                                                        },
                                                        {
                                                          "event_id": "aaff501d-5f3c-4fd1-b95c-70d4e79d6dc7",
                                                          "title": "[구립증산도서관] 가족열람실 5월 북큐레이션 [같이 놀아요, 가족]",
                                                          "genre": "교육/체험",
                                                          "posterImage": "https://culture.seoul.go.kr/cmmn/file/getImage.do?atchFileId=3caa641c5c5b4130b58755689586bf6d&thumb=Y"
                                                        },
                                                        {
                                                          "event_id": "3376515d-b72c-4f23-8a82-c85bb531d6a0",
                                                          "title": "[구립증산도서관] 보호자 게임리터러시 교육 [게임시대를 살고있는 부모는 이렇게 키운다]",
                                                          "genre": "교육/체험",
                                                          "posterImage": "https://culture.seoul.go.kr/cmmn/file/getImage.do?atchFileId=c8814b81224349a59001b4c74e7b3e60&thumb=Y"
                                                        },
                                                        {
                                                          "event_id": "7caca271-9fc1-4a1b-8bfb-bf8542f04590",
                                                          "title": "[구립증산도서관] 스마트 아카데미 수강생 모집 (상반기)",
                                                          "genre": "교육/체험",
                                                          "posterImage": "https://culture.seoul.go.kr/cmmn/file/getImage.do?atchFileId=715ea970b5504d2db2ce97d051f294f5&thumb=Y"
                                                        },
                                                        {
                                                          "event_id": "fef02dd8-fed8-46c5-9be2-f4c6d4ac7ed6",
                                                          "title": "[구립증산도서관] 책 따라 걷는 도서관 5월 원화 전시 [백 개의 꽃씨와 쥐]",
                                                          "genre": "전시/미술",
                                                          "posterImage": "https://culture.seoul.go.kr/cmmn/file/getImage.do?atchFileId=837c3ea57b184aab9350a834a60becd2&thumb=Y"
                                                        },
                                                        {
                                                          "event_id": "1c65f939-22dc-431a-b26d-768301ba33fa",
                                                          "title": "[구립증산도서관] 책이랑 놀자, 이렇게! 5월 프로그램",
                                                          "genre": "교육/체험",
                                                          "posterImage": "https://culture.seoul.go.kr/cmmn/file/getImage.do?atchFileId=85b88405dae14b8ba20d2aa832b33ce0&thumb=Y"
                                                        },
                                                        {
                                                          "event_id": "2ba8039c-60c7-4629-9cb0-f9716d99a739",
                                                          "title": "[구립증산도서관] 테마로 즐기는 도서관 - 5월 테마전시체험전 [도서관 고양이: 두 번째 이야기]",
                                                          "genre": "전시/미술",
                                                          "posterImage": "https://culture.seoul.go.kr/cmmn/file/getImage.do?atchFileId=70cf549668794862a581af57347920ec&thumb=Y"
                                                        },
                                                        {
                                                          "event_id": "0cfaa328-6c49-4fcd-9f71-ce8ad244e9f8",
                                                          "title": "[구산동도서관마을] 「SF영화 속 우주과학 빼먹기」 루카 저자와의 만남 : 과학적 상상력을 넓히는 시간",
                                                          "genre": "교육/체험",
                                                          "posterImage": "https://culture.seoul.go.kr/cmmn/file/getImage.do?atchFileId=1b69a993edef4df6ae0c7e3e827820aa&thumb=Y"
                                                        },
                                                        {
                                                          "event_id": "26c1fe50-6d3d-458e-8acf-f92224d9b512",
                                                          "title": "[구산동도서관마을] 「도서관이 된 마을, 마을이 된 도서관」 출간 기념 북토크",
                                                          "genre": "교육/체험",
                                                          "posterImage": "https://culture.seoul.go.kr/cmmn/file/getImage.do?atchFileId=fe0554484d304abda5a7305fae2de3c5&thumb=Y"
                                                        },
                                                        {
                                                          "event_id": "4e4909da-80bd-4b50-a7f3-369130475371",
                                                          "title": "[구산동도서관마을] 「에브리웨어 경제학」 김경곤 저자와의 만남(작가힙톡) : 세상의 흐름을 읽는 경제 문해력",
                                                          "genre": "교육/체험",
                                                          "posterImage": "https://culture.seoul.go.kr/cmmn/file/getImage.do?atchFileId=1f42d123faa84bf2a969b01c71ccc4d6&thumb=Y"
                                                        }
                                                      ],
                                                      "error": null
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    public ApiResponse<List<PreferenceCategoriesResponse>> getPreferenceCategories() {
        return ApiResponse.success(preferenceService.getPreferenceCategories());
    }

    @PostMapping("/api/preferences")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "취향 입력",
            description = "Stores selected seed event IDs and creates personalized recommendations before returning a response.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CreatePreferenceApiResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "success": true,
                                              "data": [
                                                "04ba876b-fc95-4b84-94ef-589cb670459c",
                                                "06d24430-f141-4d3a-a1be-712c31510269",
                                                "2541d62f-e5d9-4334-8199-dbdf9ffbe63e",
                                                "29175e70-288e-4b2a-91d5-b555c68c0093",
                                                "2d68849a-f43b-44bc-88e3-24f839869978"
                                              ],
                                              "error": null
                                            }
                                            """
                            )
                    )
            ),
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Preferences and personalized recommendations created successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = CreatePreferenceApiResponse.class),
                                    examples = @ExampleObject(
                                            value = """
                                                    {
                                                      "success": true,
                                                      "data": [
                                                "04ba876b-fc95-4b84-94ef-589cb670459c",
                                                "06d24430-f141-4d3a-a1be-712c31510269",
                                                "2541d62f-e5d9-4334-8199-dbdf9ffbe63e",
                                                "29175e70-288e-4b2a-91d5-b555c68c0093",
                                                "2d68849a-f43b-44bc-88e3-24f839869978"
                                                      ],
                                                      "error": null
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "Invalid, duplicate, or unavailable preference event IDs"
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication is required")
            }
    )
    public ApiResponse<List<UUID>> createPreferences(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody ApiResponse<List<UUID>> request) {
        preferenceService.createPreferences(userDetails.id(), request.data());
        return ApiResponse.success(request.data());
    }

    private record PreferenceCategoriesApiResponse(
            boolean success,
            @ArraySchema(
                    minItems = 20,
                    maxItems = 20,
                    schema = @Schema(implementation = PreferenceCategoriesResponse.class)
            )
            List<PreferenceCategoriesResponse> data,
            Object error
    ) {
    }

    private record CreatePreferenceApiResponse(
            boolean success,
            @ArraySchema(
                    minItems = 5,
                    maxItems = 20,
                    schema = @Schema(type = "string", format = "uuid", example = "04ba876b-fc95-4b84-94ef-589cb670459c")
            )
            List<UUID> data,
            Object error
    ) {
    }
}
