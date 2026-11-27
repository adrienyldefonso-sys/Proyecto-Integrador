from django.urls import path
from . import views
from .views_alertas import AlertasView
from .views_metricas import MetricasView
from .views_revision import RevisarDiagnosticoView, RevisionesListView, SesionView
from .views_usuarios import (
    UsuarioCerrarSesionesView,
    UsuarioDetalleView,
    UsuarioEstadoView,
    UsuariosListView,
)
urlpatterns = [
    path("parcelas/", views.ParcelaListView.as_view(), name="parcelas"),
    path(
        "parcelas/<str:parcela_id>/historial/",
        views.HistorialParcelaView.as_view(),
        name="historial-parcela",
    ),
    path("alertas/", AlertasView.as_view(), name="alertas"),
    path("metricas/", MetricasView.as_view(), name="metricas"),
    path("sesion/", SesionView.as_view(), name="sesion"),
    path("revisiones/", RevisionesListView.as_view(), name="revisiones"),
    path(
        "diagnosticos/<str:diagnostico_id>/revision/",
        RevisarDiagnosticoView.as_view(),
        name="revisar-diagnostico",
    ),
    path("usuarios/", UsuariosListView.as_view(), name="usuarios"),
    path("usuarios/<str:usuario_id>/", UsuarioDetalleView.as_view(), name="usuario-detalle"),
    path("usuarios/<str:usuario_id>/estado/", UsuarioEstadoView.as_view(), name="usuario-estado"),
    path(
        "usuarios/<str:usuario_id>/cerrar-sesiones/",
        UsuarioCerrarSesionesView.as_view(),
        name="usuario-cerrar-sesiones",
    ),
]