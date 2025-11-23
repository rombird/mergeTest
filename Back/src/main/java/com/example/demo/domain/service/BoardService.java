package com.example.demo.domain.service;

import com.example.demo.domain.dto.BoardDto;
import com.example.demo.domain.entity.BoardEntity;
import com.example.demo.domain.repository.BoardRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// DTO -> Entity (Entity 클래스에서 할거임)
// Entity -> DTO(DTO클래스에서 할거임)

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;

    public void save(BoardDto boardDto){

        String cleanText = Jsoup.parse(boardDto.getBoardContents()).text();
        boardDto.setBoardContents(cleanText);

        BoardEntity boardEntity = BoardEntity.toSaveEntity(boardDto);
        boardRepository.save(boardEntity);     // boardRepository는 파라미터로 entity객체만 받음. 그래서 dto <-> entity 작업이 필요

    }

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

    // 게시글 클릭 시 DB에 있는 내용 보여주기
    public BoardDto findById(Long id){
        Optional<BoardEntity> optionalBoardEntity = boardRepository.findById(id);
        if(optionalBoardEntity.isPresent()){
            BoardEntity boardEntity = optionalBoardEntity.get();
            BoardDto boardDto = BoardDto.toBoardDto(boardEntity);
            return boardDto;
        }else {
            return null;
        }
    }

}
