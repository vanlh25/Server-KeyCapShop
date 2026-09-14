package com.vn.keycap_server.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.vn.keycap_server.dto.response.review.ReviewResponse;
import com.vn.keycap_server.dto.response.review.UserReviewInfo;
import com.vn.keycap_server.modal.Review;
import com.vn.keycap_server.modal.ReviewReply;
import com.vn.keycap_server.modal.User;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "user", source = "user")
    @Mapping(target = "rating", source = "rating")
    @Mapping(target = "content", source = "content")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "imageUrls", source = "imageUrls")
    @Mapping(target = "reply", source = "reply")
    @Mapping(target = "isHidden", source = "isHidden")
    ReviewResponse reviewToReviewResponse(Review review);

    @Mapping(target = "fullName", source = "fullName")
    @Mapping(target = "avatar", source = "avatarMedia.secureUrl")
    UserReviewInfo userToUserReviewInfo(User user);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "adminName", source = "user.fullName")
    @Mapping(target = "content", source = "content")
    @Mapping(target = "createdAt", source = "createdAt")
    ReviewResponse.ReplyResponse replyToReplyResponse(ReviewReply reply);
}
