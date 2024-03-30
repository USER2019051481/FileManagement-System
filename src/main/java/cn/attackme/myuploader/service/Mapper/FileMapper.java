package cn.attackme.myuploader.service.Mapper;


import cn.attackme.myuploader.dto.FileDTO;
import cn.attackme.myuploader.entity.FileEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {})
public interface FileMapper {
    FileMapper INSTANCT = Mappers.getMapper(FileMapper.class) ;
    @Mapping(target = "upload_time",source = "uploadTime")
    @Mapping(target = "extractKeys_data",source = "extractKeysData")
    FileEntity dto2entity(FileDTO fileDTO) ;

    @Mapping(target = "uploadTime",source = "upload_time")
    @Mapping(target = "extractKeysData",source = "extractKeys_data")
    FileDTO entity2dto(FileEntity fileEntity) ;
}
