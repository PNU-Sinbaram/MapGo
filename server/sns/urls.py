from django.conf.urls import include
from django.urls import path

from .views import UserViewSet, PostViewSet, CommentViewSet


from rest_framework import routers

router = routers.DefaultRouter()
router.register('user/(?P<userID>\d+)', UserViewSet, basename='user')
router.register('user', UserViewSet, basename='user')
router.register('post/(?P<postID>\d+)/comment', CommentViewSet, basename='comment')
router.register('post/(?P<author>\w+)', PostViewSet, basename = 'post')
router.register('post', PostViewSet, basename='post')

urlpatterns = [
        path('', include(router.urls)),
]
