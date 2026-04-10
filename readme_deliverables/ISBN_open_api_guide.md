

2020\. 3\.

국립중앙도서관

**목 차**

**1\. Open API 개요	3**

1.1 Open API 정의	3

1.2 Open API 동작원리	3

1.3 Open API 표	3

**2\. 자료검색	4**

2.1 일반검색	4

2.2 상세검색	7

2.3 출력 결과 필드	8

2.4 에러 메시지	9

**3\. ISBN서지정보	10**

3.1 요청변수	10

3.2 출력 결과 항목	11

3.3 에러 메시지	12

1\. Open API 개요

1.1 Open API 정의  
Open API란 일반적으로 웹 서비스(Web Services)형태로써 특정 기능 혹은 콘텐츠 서비스를 위해 외부에 접근 방법을 공개한 형태를 개방형 인터페이스(이하OPENAPI)라 합니다.

1.2 Open API 동작원리  
API의 동작 단계는 이용자 입장에서 요청(request)하는 단계와, 결과(response)값을 받아 해석(parse)하는 단계로 구성됩니다.  
이용자는 해당 Open API 주소 뒤에 매개변수 값을 붙이는 GET 방식 또는 페이지 폼 안의 변수에 대한 POST 전달방식을 통하여 API 매개변수 값을 전달하고, 서버에서 처리 수행한 결과를 반환 받습니다.  
\- GET 방식 매개변수 구성 예시: URL?변수=변수값1&변수2=변수값2…..(‘&’ 표기로 변수구분)

1.3 Open API 표

| 분류 | API |
| ----- | ----- |
| 자료검색 | 일반검색 |
|  | 상세정보요청 |
|  | ISBN 서지정보 |

2\. 자료검색

2.1 일반검색  
소장 자료를 조회할 수 있는 기능을 제공합니다.

※ utf-8 인코딩  
※ Kwd 값과 상세검색 값은 혼용하여 사용이 불가능 합니다.  
※ 검색조건 및 맵핑되는 검색어가 하나이상 들어가야 합니다.

2.1.1 일반검색 요청URL(request url)  
[https://www.nl.go.kr/NL/search/openApi/search.do](https://www.nl.go.kr/NL/search/openApi/search.do)

- 온프라인자료(구:소장정보)  
  [https://www.nl.go.kr/NL/search/openApi/search.do?systemType](https://www.nl.go.kr/NL/search/openApi/search.do?systemType)\=오프라인자료  
- 온라인자료(구:디지털화자료) 

https://www.nl.go.kr/NL/search/openApi/search.do?systemType=온라인자료

- 정부간행물(구:정부간행물) 자료검색

[https://www.nl.go.kr/NL/search/openApi/search.do?govYn=Y](https://www.nl.go.kr/NL/search/openApi/search.do?govYn=Y)

2.1.2 소장정보요청 변수 (request parameter)

| NO | 변수명 | TYPE | 값 설명 |
| :---: | :---- | :---- | :---- |
| 1 | key | String(필수) | 발급키 |
| 2 | srchTarget |  | total (전체), title (제목),  author (저자), publisher (발행자), cheonggu (청구기호), 생략시 전체 |
| 3 | kwd | String | 검색어 |
| 4 | pageNum | Integer(필수) | 현재페이지 |
| 5 | pageSize | Integer(필수) | 쪽당출력건수 (기본 10건) |
| 6 | systemType | String | 오프라인자료 (구: 소장정보) 온라인자료 (구: 디지털화자료) |
| 7 | category | String | \[카테고리(자료유형)\] 도서, 고서/고문서, 학위논문, 잡지/학술지, 신문, 기사, 멀티미디어, 장애인자료, 외부연계자료, 웹사이트 수집, 기타, 해외한국관련기록물 |
| 8 | lnbTypeName (멀티미디어,장애인자료) | String | \[멀티미디어 하위분류\] 오디오북, 음악자료, 지도자료, 이미지/사진, 컴퓨터파일, 영상자료, 마이크로자료 \[장애인자료 하위분류\] 점자자료, 장애인대체자료 |
| 9 | offerDcode2s (소장원문) | String | \[자료제공DB별 자체분류 2차\_명\] CH4G (한국고전적종합목록) CH4A (고지도) CH48 (신문학대표소설) CH4Q (학술회원자료) CH4E (어린이, 청소년 관련 자료) CH47 (문화체육관광부 발간자료) CH4M (독도관련자료) CH4I (우수학술도서) CH43 (관보(1894\~1945)) CH44 (한글판 고전소설) CH4R (교과서\]) CH4B (연속간행물 귀중본) CH49 (신문(\~1950)) CH4P (정부간행물) CH4T (악보) CH4F (일본어자료(\~1945)) CH45 (단행자료) CH41 (고서) CH4L (한국관련외국어자료) CH4D (인문과학분야 박사학위논문) CH4J (한국고전백선) CH4K (국내발간 한국관련 외국어자료) |
| 10 | sort |  | 정렬 (생략시 : 정확도순) ititle (제목), iauthor (저자), ipublisher (발행처) ipub\_year (발행년도), cheonggu (청구기호) |
| 11 | desc |  | asc (오름차순), desc (내림차순) |
| 12 | apiType |  | xml, json |
| 13 | licYn |  | 원문이용방법(원문저작권) S (\[국립중앙도서관,협약도서관\]-인쇄 시 과금) F (\[국립중앙도서관,협약도서관\]-열람,인쇄시 과금) Y (\[국립중앙도서관,협약공공도서관,정기이용증소지자\]-무료) L (\[국립중앙도서관\]-무료) N (\[관외이용\]-무료) C (\[국립중앙도서관,작은도서관\]-무료) U (\[국립중앙도서관,국립어린이청소년도서관\]-무료) T (\[국립중앙도서관,국립어린이청소년도서관,작은도서관\]-무료) R (\[국립중앙도서관,정기이용증소지자\]-무료) D (\[국립중앙도서관,국립어린이청소년도서관,국립세종도서관\]-무료) A (\[국립중앙도서관,국립어린이청소년도서관,국립세종도서관,정기이용증소지자\]-무료) |
| 14 | govYn | String | Y (정부간행물) |

   
Example1\>검색어 : ‘토지’, 카테고리 도서  
[https://www.nl.go.kr/NL/search/openApi/search.do?key=\[발급된키값\]\&apiType=xml\&srchTarget=total\&kwd=%ED%86%A0%EC%A7%80\&pageSize=10\&pageN](https://www.nl.go.kr/NL/search/openApi/search.do?key=[발급된키값]&apiType=xml&srchTarget=total&kwd=토지&pageSize=10&pageN)um=1\&sort=\&category=%EB%8F%84%EC%84%9C

Example2\>검색어 : ‘토지’, 원문이용방법 관외이용(무료)  
[https://www.nl.go.kr/NL/search/openApi/search.do?key=\[발급된키값\]\&apiType=xml\&srchTarget=total\&kwd=%ED%86%A0%EC%A7%80\&pageSize=10\&pageNum=1\&li](https://www.nl.go.kr/NL/search/openApi/search.do?key=[발급된키값]&apiType=xml&srchTarget=total&kwd=토지&pageSize=10&pageNum=1&li)cYn=N

Example3\> 자료제공DB별 자체분류 2차\_명 신문대표소설  
https://www.nl.go.kr/NL/search/openApi/search.do?key=\[발급된키값\]\&apiType=xml\&detailSearch=true\&offerDbcode2s=CH48\&pageSize=10\&pageNum=1

2.2 상세검색  
2.2.1 소장정보 요청 변수 (request parameter)  
\- 공통

| NO | 변수명 | TYPE | 값 설명 |
| :---: | :---- | :---- | :---- |
| 1 | detailSearch | boolean | \[상세검색 사용유무\] true / false(기본) |
| 2 | f1 | String | 검색조건1 total (전체), title (표제/논문명) keyword (키워드), author (저자) publisher (발행자) |
| 3 | v1 | String | 키워드1  |
| 4 | and1 | String | AND / OR / NOT (검색어 연결조건1) |
| 5 | f2 | String | 검색조건2 total (전체), title (표제/논문명) keyword (키워드), author (저자) publisher (발행자) |
| 6 | v2 | String | 키워드2 |
| 7 | and2 | String | AND / OR / NOT (검색어 연결조건2) |
| 8 | f3 | String | 검색조건3 total (전체), title (표제/논문명) keyword (키워드), author (저자) publisher (발행자) |
| 9 | v3 | String | 키워드3 |
| 10 | and3 | String | AND / OR / NOT (검색어 연결조건3) |
| 11 | f4 | String | 검색조건4 total (전체), title (표제/논문명) keyword (키워드), author (저자) publisher (발행자) abs\_keyword (초록) toc\_keyword (목차) |
| 12 | v4 | String | 키워드4 |
| 13 | and4 | String | AND / OR / NOT (검색어 연결조건4) |
| 14 | isbnOp |  | isbn, issn (isbn 구분) |
| 15 | isbnCode |  | isbn(issn) 코드값 |
| 16 | guCode3 |  | 별치기호 |
| 17 | guCode4 |  | 분류기호 |
| 18 | guCode5 |  | 도서 |
| 19 | guCode6 |  | 권책 |
| 20 | guCode7 |  | 한국대학명 |
| 21 | guCode8 |  | 한국정부기관명 |
| 22 | gu10 |  | 판종유형/판종 |
| 23 | guCode11 |  | CIP제어번호 |
| 24 | gu12 |  | 본문언어 |
| 25 | gu13 |  | 요약언어 |
| 26 | gu14 |  | 간행빈도 |
| 27 | sYear |  | 발행년도 시작일 |
| 28 | eYear |  | 발행년도 종료일 |
| 29 | gu2 |  | \[분류기호\] kdc (한국십진분류표) kdcp (한국십진분류표-박봉석편) ddc (듀이십진분류표) cec (조선총독부 신서부분류표) cwc (조선총독부 양서부분류표) coc (조선총독부 고서부분류표) gpo (정부문서분류번호) |
| 30 | guCode2 |  | 분류코드값 |

Example1\> 제목: ’토지’, 저자: ’박경리’  
[https://www.nl.go.kr/NL/search/openApi/search.do?key=\[발급된키값\]\&kwd=%ED%86%A0%EC%A7%80\&detailSearch=true\&f1=title\&v1=%ED%86%A0%EC%A7%80\&f2=author\&v2=%EB%B0%95%EA%B2%BD%EB%](https://www.nl.go.kr/NL/search/openApi/search.do?key=[발급된키값]&kwd=토지&detailSearch=true&f1=title&v1=토지&f2=author&v2=박경%EB%25)A6%AC

Example1\> ISBN 검색  
[https://www.nl.go.kr/NL/search/openApi/search.do?key=\[발급된키값\]\&detailSearch=true\&isbnOp=isbn\&isbnCode=89849](https://www.nl.go.kr/NL/search/openApi/search.do?key=[발급된키값]&detailSearch=true&isbnOp=isbn&isbnCode=89849)93727

2.3 출력 결과 필드 (response field)  
2.3.1 소장정보 출력 결과 필드(response field)

| NO | 결과 |  | 설명 |
| :---- | :---- | :---- | :---- |
| 1 | kwd |  | 검색어 |
| 2 | category |  | 카테고리 |
| 3 | pageNum |  | 현재페이지 |
| 4 | pageSize |  | 쪽당출력건수 (기본 10건) |
| 5 | sort |  | 정렬 |
| 6 | total |  | 검색건수 |
| 7 | title\_info |  | 표제 리스트 |
| 8 | type\_name |  | 자료유형 |
| 9 | place\_info |  | 자료있는곳명칭(본관) |
| 10 | author\_info |  | 저작자 |
| 11 | pub\_info |  | 발행자 |
| 12 | menu\_name |  | 메뉴명 |
| 13 | media\_name |  | 매체구분 |
| 14 | manage\_name |  | 자료있는곳 명 |
| 15 | pub\_year\_info |  | 발행년도사항 |
| 16 | control\_no |  | 제어번호 |
| 17 | doc\_yn |  | 원문유무 |
| 18 | org\_link |  | 원문링크 |
| 19 | id |  | 종키 |
| 20 | type\_code |  | 자료유형코드 |
| 21 | lic\_yn |  | 저작권유무 |
| 22 | lic\_text |  | 저작권설명 |
| 23 | reg\_date |  | 비치일 |
| 24 | detail\_link |  | 상세페이지경로 |
| 25 | isbn |  | ISBN |
| 26 | call\_no |  | 청구기호 |
| 27 | kdc\_code\_1s |  | 동양서분류기호 대분류 코드 |
| 28 | kdc\_name\_1s |  | 동양서분류기호 대분류 명칭 |

2.4 에러 메시지  
2.4.1 에러메시지

| 000 : SYSTEM ERROR SYSTEM 오류 |
| :---- |
| 010 : NO KEY VALUE 인증키값 누락 |
| 011 : INVALID KEY 유효하지 않은 인증키 |
| 012 : DATA LIMIT 500 검색결과 이동시 500건 제한(500건 이후 데이터 조회불가) |
| 013 : CATEGORY ERROR 카테고리값 입력오류  |
| 014 : PARAMETER VALIDATION ERROR 파라메터 입력값 오류  |
| 015 : REQUIRED PARAMETER MISSING 필수 파라메터 입력 오류(검색어 or 상세검색) |
| 101: SEARCH ERROR 검색서버 오류 |

3\. ISBN 서지정보  
일반검색 요청URL(request url)  
[https://www.nl.go.kr/seoji/SearchApi.do](https://www.nl.go.kr/seoji/SearchApi.do)   

- 미납본 목록 (구:출판예정도서)  
- [https://www.nl.go.kr/seoji/SearchApi.do](https://www.nl.go.kr/seoji/SearchApi.do)?deposit\_yn=N 

3.1 요청 변수 (request parameter)

| NO | 요청변수 | TYPE | 색인방법 | 값 설명 |
| :---: | :---- | ----- | ----- | :---- |
| 1 | cert\_key | String(필수) |  | 인증키 |
| 2 | result\_style | String(필수) |  | 결과 형식 (json, xml) |
| 3 | page\_no | Integer(필수) |  | 현재 쪽번호(페이지 1부터 시작) |
| 4 | page\_size | Integer(필수) |  | 쪽당 출력건수 |
| 5 | isbn | String | 우절단 검색 | ISBN |
| 6 | set\_isbn | String | 우절단 검색 | SET ISBN |
| 7 | ebook\_yn | String | 일치검색 | 전자책여부 Y, N |
| 8 | title | String | 형태소 \+ ngram | 본표제 |
| 9 | start\_publish\_date | String | 범위검색 | 발행예정일  시작(8자리, yyyymmdd) |
| 10 | end\_publish\_date | String | 범위검색 | 발행예정일  끝(8자리, yyyymmdd) |
| 11 | cip\_yn | String | 일치검색 | CIP 신청여부 Y, N |
| 12 | deposit\_yn | String | 일치검색 | 납본유무 Y, N |
| 13 | series\_title | String | 형태소 \+ ngram | 총서명 |
| 14 | publisher | String | 형태소 \+ ngram | 발행처명 |
| 15 | author | String | 형태소 \+ ngram | 저자 |
| 16 | form | String | 일치검색 | 형태사항 (종이책, 혼합자료, 전자책, 오디오북, 기타 전자출판물, 다양한 제본형태, 다양한 형식혼합 세트) |
| 17 | sort | String |  | 정렬  PUBLISH\_PREDATE INPUT\_DATE INDEX\_TITLE INDEX\_PUBLISHER |
| 18 | order\_by | String |  | 정렬방식 ASC, DESC |

※ API 샘플URL  
https://www.nl.go.kr/seoji/SearchApi.do?cert\_key=\[발급된키값\]\&result\_style=json\&page\_no=1\&page\_size=10\&start\_publish\_date=20220509\&end\_publish\_date=20220509

3.2 출력 결과 항목

| NO | 출력항목 | TYPE | 값 설명 |
| ----- | :---- | ----- | :---- |
| 1 | PAGE\_NO | String | 현재 쪽번호 |
| 2 | TOTAL\_COUNT | String | 전체 출력수 |
| 3 | TITLE | String | 표제 |
| 4 | VOL | String | 권차 |
| 5 | SERIES\_TITLE | String | 총서명 |
| 6 | SERIES\_NO | String | 총서편차 |
| 7 | AUTHOR | String | 저자 |
| 8 | EA\_ISBN | String | ISBN |
| 9 | EA\_ADD\_CODE | String | ISBN 부가기호 |
| 10 | SET\_ISBN | String | 세트 ISBN |
| 11 | SET\_ADD\_CODE | String | 세트 ISBN 부가기호 |
| 12 | SET\_EXPRESSION | String | 세트표현 (세트, 전2권.) |
| 13 | PUBLISHER | String | 발행처 |
| 14 | EDITION\_STMT | String | 판사항 |
| 15 | PRE\_PRICE | String | 예정가격 |
| 16 | KDC | String | 한국십진분류 |
| 17 | DDC | String | 듀이십진분류 |
| 18 | PAGE | String | 페이지 |
| 19 | BOOK\_SIZE | String | 책크기 |
| 20 | FORM | String | 발행제본형태 |
| 21 | PUBLISH\_PREDATE | String | 출판예정일 |
| 22 | SUBJECT | String | 주제 |
| 23 | EBOOK\_YN | String | 전자책여부 (Y: 전자책, N : 인쇄책) |
| 24 | CIP\_YN | String | CIP 신청여부 (Y: CIP신청, N: CIP신청안함) |
| 25 | CONTROL\_NO | String | CIP 제어번호 |
| 26 | TITLE\_URL | String | 표지이미지 URL |
| 27 | BOOK\_TB\_CNT\_URL | String | 목차 |
| 28 | BOOK\_INTRODUCTION\_URL | String | 책소개 |
| 29 | BOOK\_SUMMARY\_URL | String | 책요약 |
| 30 | PUBLISHER\_URL | String | 출판사 홈페이지 URL |
| 31 | INPUT\_DATE | String | 등록날짜 |
| 32 | UPDATE\_DATE | String | 수정날짜 |

※ 제공서비스에 따라 출력결과 필드는 제한될 수 있습니다.

3.2 에러 메시지

| 에러코드 | 설명 |
| :---: | ----- |
| 000 | 시스템오류 |
| 010 | 인증키값 누락 |
| 011 | 유효하지 않은 인증키 |
| 015 | 필수 파라메터 입력 누락 |

