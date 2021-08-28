from django.conf.urls import include
from django.urls import path

from .views import UserViewSet, PostViewSet, CommentViewSet, LikeViewSet


from rest_framework import routers

likeview = LikeViewSet.as_view({
    "post": "create",
    "delete": "destroy"
})

router = routers.DefaultRouter()
#router.register('user/(?P<userID>\d+)', UserViewSet, basename='user')
router.register('user', UserViewSet, basename='user')
router.register('post/(?P<postID>\d+)/comment', CommentViewSet, basename='comment')
# router.register('post/(?P<postID>\d+)/like', LikeViewSet, basename='like')
router.register('post/(?P<author>\w+)', PostViewSet, basename = 'post')
router.register('post', PostViewSet, basename='post')

urlpatterns = [
        path('', include(router.urls)),
        path('post/<int:postID>/like/', likeview),
]
