from django.urls import path

from . import views

urlpatterns = [
    path("parcelas/", views.ParcelaListView.as_view(), name="parcelas"),
    path(
        "parcelas/<uuid:parcela_id>/historial/",
        views.HistorialParcelaView.as_view(),
        name="historial-parcela",
    ),
]