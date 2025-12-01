package com.example.demo.service;

import com.example.demo.domain.dto.BoardDto;
import com.example.demo.domain.dto.BoardFileDto;
import com.example.demo.domain.entity.BoardEntity;
import com.example.demo.domain.entity.BoardFileEntity;
import com.example.demo.domain.repository.BoardFileRepository;
import com.example.demo.domain.repository.BoardRepository;
import jakarta.persistence.Id;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriUtils;

import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

// DTO -> Entity (Entity 클래스에서 할거임)
// Entity -> DTO(DTO클래스에서 할거임)

@Slf4j
@Service
public class BoardService {

    private final BoardRepository boardRepository;
    private final BoardFileRepository boardFileRepository;

    @Value("${file.dir}")       // 파일 저장 경로
    private String fileDir;

    // 파일만 담는 생성자
    public BoardService(BoardRepository boardRepository, BoardFileRepository boardFileRepository){
        this.boardRepository = boardRepository;
        this.boardFileRepository = boardFileRepository;
    }

    // 게시글 저장
    // 게시글 저장 메서드: LazyInitializationException 문제 해결
    @Transactional
    public BoardDto save(BoardDto boardDto) throws IOException {

        // 1. HTML 태그 제거 (Jsoup 사용)
//        if (boardDto.getBoardContents() != null) {
//            String cleanText = Jsoup.parse(boardDto.getBoardContents()).text();
//            boardDto.setBoardContents(cleanText);
//        }

        // 1. HTML 태그 클리닝 및 정리(IMG태그를 포함한 서식 태그 허용)
        if(boardDto.getBoardContents() != null){

            // 텍스트 서식 태그와 img태그를 허용하고 나머지는 제거
            String cleanText = Jsoup.clean(boardDto.getBoardContents(), Safelist.basicWithImages());

            boardDto.setBoardContents(cleanText);
        }

        // 2. 파일 미첨부 시
        if (boardDto.getFileUpload() == null || boardDto.getFileUpload().isEmpty()) {
            BoardEntity boardEntity = BoardEntity.toSaveEntity(boardDto);
            boardRepository.save(boardEntity);
            return BoardDto.toBoardDto(boardEntity); // Lazy Loading 문제 없음
        }

        // 3. 파일 첨부 시 (Lazy Loading 문제 해결 포함)
        else {
            // a. BoardEntity 저장 (fileAttached = 1로 설정된 Entity)
            BoardEntity boardEntity = BoardEntity.toSaveFileEntity(boardDto);
            boardRepository.save(boardEntity);

            List<BoardFileEntity> savedFileEntityList = new ArrayList<>();

            for (MultipartFile boardFile : boardDto.getFileUpload()) {
                String originalFilename = boardFile.getOriginalFilename();
                String storedFilename = System.currentTimeMillis() + "_" + originalFilename;
                String savePath = fileDir + storedFilename;

                long fileSize = boardFile.getSize();


                // 파일 시스템에 저장
                boardFile.transferTo(new File(savePath));

                // BoardFileEntity 생성 및 관계 설정
                BoardFileEntity boardFileEntity = BoardFileEntity.toBoardFileEntity(boardEntity, originalFilename, storedFilename, fileSize);

                // BoardFileEntity 저장
                boardFileRepository.save(boardFileEntity);

                // 메모리상의 리스트에 추가
                savedFileEntityList.add(boardFileEntity);
            }

            // 이렇게 하면 toBoardDto 호출 시 DB 접근 없이 메모리의 파일 리스트를 사용
            boardEntity.setBoardFileEntityList(savedFileEntityList);

            // c. DTO 변환 및 반환
            return BoardDto.toBoardDto(boardEntity);
        }
    }

    @Transactional
    public List<BoardDto> findAll(){
        List<BoardEntity> boardEntityList = boardRepository.findAll();

        List<BoardDto> boardDtoList = new ArrayList<>();

        for(BoardEntity boardEntity: boardEntityList){
            boardDtoList.add(BoardDto.toBoardDto(boardEntity));
        }
        return boardDtoList;
    }

    // 조회수 올리기
    @Transactional
    public void updateHits(Long id){
        boardRepository.updateHits(id);
    }

    @Transactional
    // 게시글 클릭 시 DB에 있는 내용 보여주기
    public BoardDto findById(Long id){
        Optional<BoardEntity> optionalBoardEntity = boardRepository.findById(id);
        if(optionalBoardEntity.isPresent()){
//            return BoardDto.toBoardDto(optionalBoardEntity.get());

            BoardEntity boardEntity = optionalBoardEntity.get();
            BoardDto boardDto = BoardDto.toBoardDto(boardEntity);
            return boardDto;
        }else {
            return null;
        }
    }

    // ################################################################
    // 게시글 수정하고싶다
    // ################################################################

    @Transactional
    public BoardDto update(BoardDto boardDto, List<MultipartFile> newFiles, List<Long> deleteFileIds) throws IOException {
        // 1. 기존 게시글 조회
        BoardEntity boardEntity = boardRepository.findById(boardDto.getId())
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        // 2. 비밀번호 검증
        if (!boardEntity.getBoardPass().equals(boardDto.getBoardPass())){
            // 비밀번호가 틀릴 경우 예외 발생
            throw new IllegalArgumentException("게시글 수정 실패: 비밀번호가 일치하지 않습니다");
        }

        // 3. 텍스트 정보 업데이트 (제목, 내용)
        // 불필요한 HTML 태그 제거
        if (boardDto.getBoardContents() != null) {
            String cleanText = Jsoup.clean(boardDto.getBoardContents(), Safelist.basicWithImages());

            boardEntity.updateText(boardDto.getBoardTitle(), cleanText);
        }

        // 4. 파일 삭제 처리
        if (deleteFileIds != null && !deleteFileIds.isEmpty()) {
            for (Long fileId : deleteFileIds) {
                // DB에서 파일 정보 조회
                BoardFileEntity fileEntity = boardFileRepository.findById(fileId).orElse(null);

                if (fileEntity != null) {   // 삭제하는 파일이 파일엔티티에 담겨있다면?
                    // 로컬 디스크에서 파일 삭제
                    String savePath = fileDir + fileEntity.getStoredFilename();
                    File file = new File(savePath);
                    if (file.exists()) {
                        if(!file.delete()){
                            log.error("파일 삭제 실패: {}", savePath);
                        }
                    }
                    // DB에서 파일 데이터 삭제
                    boardFileRepository.delete(fileEntity);
                }
            }
        }

        // 5. 새 파일 추가 처리
        if (newFiles != null && !newFiles.isEmpty()) {
            for (MultipartFile boardFile : newFiles) {
                if (!boardFile.isEmpty()) {
                    String originalFilename = boardFile.getOriginalFilename();
                    String storedFilename = System.currentTimeMillis() + "_" + originalFilename;
                    String savePath = fileDir + storedFilename;

                    Long fileSize = boardFile.getSize();    // 파일 크기 가져오기

                    boardFile.transferTo(new File(savePath));

                    BoardFileEntity boardFileEntity = BoardFileEntity.toBoardFileEntity(boardEntity, originalFilename, storedFilename, fileSize);
                    boardFileRepository.save(boardFileEntity);
                }
            }
        }

        // 6. 파일 첨부 여부(fileAttached) 상태 업데이트
        // 현재 이 게시글에 연결된 파일 개수 확인
        // 변경 사항이 즉시 DB에 반영되도록 saveAndFlush를 호출
        // 삭제 및 추가된 파일 정보가 모두 DB에 담기기 위해서
        boardRepository.saveAndFlush(boardEntity); // 변경 사항 즉시 반영 (혹시 모를 지연 처리 방지)

        long currentFileCount = boardFileRepository.countByBoardEntityId(boardEntity.getId());

        if(currentFileCount == 0){
            boardEntity.updateFileAttached(0);
        }else{
            boardEntity.updateFileAttached(1);
        }

        return BoardDto.toBoardDto(boardEntity);
    }

    // 삭제 기능
    @Transactional
    public void delete(Long id){
        boardRepository.deleteById(id);
    }

    // 페이징 기능
    @Transactional
    public Page<BoardDto> paging(Pageable pageable){
        int page = pageable.getPageNumber();
//        int page = pageable.getPageNumber();
        int pageLimit = 10;  // 한 페이지에 보여줄 글 개수

        // 한 페이지당 pageLimit만큼 글을 보여주고 정렬 기준은 id 기준으로 내림차순 정렬
        // boardEntities -> 스프링부트JPA에서 제공하는 듯
        // page 위치에 있는 값은 0부터 시작하니까 page변수 설정 시 -1 해놓음
        Page<BoardEntity> boardEntities =
                boardRepository.findAll(PageRequest.of(page, pageLimit, Sort.by(Sort.Direction.DESC, "id")));
                                        // page -> 몇 페이지?, pageLimit -> 한 페이지에 몇 개?, Sort.By -> 정렬기준은?

        // 필요한 정보들
        System.out.println("boardEntities.getContent(): " + boardEntities.getContent());  // 요청 페이지에 해당하는 글들
        System.out.println("boardEntities.getTotalElements(): " + boardEntities.getTotalElements()); // 전체 글 갯수
        System.out.println("boardEntities.getNumber(): " + boardEntities.getNumber()); // DB로 요청한 페이지 번호
        System.out.println("boardEntities.getTotalPages(): " + boardEntities.getTotalPages()); // 전체 페이지 갯수
        System.out.println("boardEntities.getSize(): " + boardEntities.getSize()); // 한 페이지에 보여지는 글 갯수
        System.out.println("boardEntities.hasPrevious(): " + boardEntities.hasPrevious()); // 이전 페이지 존재 여부
        System.out.println("boardEntities.isFirst()" + boardEntities.isFirst()); // 첫 페이지 여부
        System.out.println("boardEntities.isLast()" + boardEntities.isLast());    // 마지막 페이지 여부

        // 목록에서 보일 것들 -> id, writer, title, hits, createdTime
        Page<BoardDto> boardDtos = boardEntities.map(board -> new BoardDto(board.getId(), board.getBoardWriter(), board.getBoardTitle(), board.getBoardHits(), board.getCreatedTime()));  // Page 객체에서 제공하는 map메서드 -> 안의 거를 하나씩 꺼내는 역할
        return boardDtos;
    }

    // 첨부파일 다운로드
    @Transactional
    public BoardFileDto fileDownloadByIndex(Long boardId, int fileIndex){

        // 게시글 조회
        BoardDto boardDto = findById(boardId);

        // 2. 유효성 검사
        if(boardDto == null || boardDto.getFileAttached() != 1 ||
            boardDto.getBoardFileDtoList() == null ||
                boardDto.getBoardFileDtoList().size() <= fileIndex
        ){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "파일을 찾을 수 없습니다");
        }

        // 3. 해당 인덱스의 파일 DTO 반환
        return boardDto.getBoardFileDtoList().get(fileIndex);

    }
}








