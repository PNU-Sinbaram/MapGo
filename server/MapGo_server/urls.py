"""MapGo_server URL Configuration

The `urlpatterns` list routes URLs to views. For more information please see:
    https://docs.djangoproject.com/en/3.2/topics/http/urls/
Examples:
Function views
    1. Add an import:  from my_app import views
    2. Add a URL to urlpatterns:  path('', views.home, name='home')
Class-based views
    1. Add an import:  from other_app.views import Home
    2. Add a URL to urlpatterns:  path('', Home.as_view(), name='home')
Including another URLconf
    1. Import the include() function: from django.urls import include, path
    2. Add a URL to urlpatterns:  path('blog/', include('blog.urls'))
"""
from django.conf import settings
from django.conf.urls import include
from django.conf.urls.static import static
from django.contrib import admin
from django.urls import path

from rest_framework import routers

from checkin.views import CheckinViewSet
from recommend.views import STCViewSet
from sns.views import UserViewSet

import os

routers = {
    "checkin": routers.DefaultRouter(),
    "recommend": routers.DefaultRouter(),
}

routers["checkin"].register('Mapgo/checkin',
                            CheckinViewSet, basename='checkin')
routers["recommend"].register('Mapgo/recommend',
                              STCViewSet, basename='recommend')

urlpatterns = [
    path('admin/', admin.site.urls),
    path('', include(routers["checkin"].urls)),
    path('Mapgo/checkin/<str:userid>/',
         CheckinViewSet.as_view({'delete': 'delete'})),
    path('', include(routers["recommend"].urls)),
    path('Mapgo/sns/', include('sns.urls')),
] + static(settings.MEDIA_URL, document_root=settings.MEDIA_ROOT)
